package com.github.pinmacaroon.dchook.util;

import com.github.pinmacaroon.dchook.Hook;
import com.github.pinmacaroon.dchook.conf.ModConfigs;
import com.github.zafarkhaja.semver.Version;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Checks GitHub releases for a newer build and optionally installs it when the server stops.
 * <ul>
 *     <li>release channel: the latest non-prerelease GitHub release, compared by semver</li>
 *     <li>alpha channel: the rolling "alpha" pre-release that CI refreshes on every push, compared by commit</li>
 * </ul>
 */
public class Updater {
    // Hook.HTTPCLIENT doesn't follow redirects, release asset downloads always redirect
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(15))
            .build();
    private static final String USER_AGENT = "dchook-updater (+https://github.com/pokeyt59/hook)";

    private record Asset(String name, URI url, String sha256) {}

    public static void start() {
        Thread thread = new Thread(Updater::check, "dchook-updater");
        thread.setDaemon(true);
        thread.start();
    }

    private static void check() {
        try {
            String channel = ModConfigs.FUNCTIONS_UPDATE_CHANNEL.strip().toLowerCase();
            boolean alpha = switch (channel) {
                case "alpha" -> true;
                case "release" -> false;
                default -> {
                    Hook.LOGGER.error("unknown update channel '{}', use 'release' or 'alpha'!", channel);
                    yield false;
                }
            };
            String repo = ModConfigs.FUNCTIONS_UPDATE_REPO.strip();

            JsonObject release = fetchRelease(repo, alpha ? "tags/alpha" : "latest");
            if (release == null) return;

            String label;
            if (alpha) {
                String remoteCommit = release.get("target_commitish").getAsString();
                if (remoteCommit.equals(localCommit())) {
                    Hook.LOGGER.info("dchook is up to date with the latest alpha build ({})", shortCommit(remoteCommit));
                    return;
                }
                label = "alpha build " + shortCommit(remoteCommit);
            } else {
                String tag = release.get("tag_name").getAsString();
                Optional<Version> remoteVersion = Version.tryParse(tag.startsWith("v") ? tag.substring(1) : tag);
                if (remoteVersion.isEmpty()) {
                    Hook.LOGGER.error("latest release tag '{}' is not a valid version number", tag);
                    return;
                }
                Version remote = remoteVersion.get().withoutBuildMetadata();
                Version local = Hook.VERSION.withoutBuildMetadata();
                if (!remote.isHigherThan(local)) {
                    if (local.isHigherThan(remote)) {
                        Hook.LOGGER.warn("you are running an unreleased version ({})! latest release is {}", local, remote);
                    } else {
                        Hook.LOGGER.info("dchook is up to date ({})", local);
                    }
                    return;
                }
                label = "version " + remote;
            }

            String mcVersion = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow()
                    .getMetadata().getVersion().getFriendlyString();
            Asset asset = findAsset(release, mcVersion);
            if (asset == null) {
                Hook.LOGGER.warn("a newer dchook {} exists, but it has no build for minecraft {}", label, mcVersion);
                return;
            }

            if (!ModConfigs.FUNCTIONS_UPDATE_AUTO) {
                Hook.LOGGER.info("a newer dchook {} is available! download it here: {}", label, asset.url());
                return;
            }

            Path pending = download(asset);
            if (pending == null) return;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> install(pending, asset.name()), "dchook-update-install"));
            Hook.LOGGER.info("downloaded dchook {}, it will be installed when the server stops", label);
        } catch (Exception e) {
            Hook.LOGGER.error("update check failed! {}:{}", e.getClass().getName(), e.getMessage());
        }
    }

    private static JsonObject fetchRelease(String repo, String which) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://api.github.com/repos/" + repo + "/releases/" + which))
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", USER_AGENT)
                .timeout(Duration.ofSeconds(30))
                .build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 404) {
            Hook.LOGGER.warn("no {} release found on github repository {}", which.equals("latest") ? "stable" : "alpha", repo);
            return null;
        }
        if (response.statusCode() != 200) {
            Hook.LOGGER.error("couldn't check for updates, github answered with status {}", response.statusCode());
            return null;
        }
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    private static Asset findAsset(JsonObject release, String mcVersion) {
        for (JsonElement element : release.getAsJsonArray("assets")) {
            JsonObject asset = element.getAsJsonObject();
            String name = asset.get("name").getAsString();
            if (!name.startsWith(Hook.MOD_ID + "-") || !name.endsWith("+fabric." + mcVersion + ".jar")) continue;
            JsonElement digest = asset.get("digest");
            String sha256 = (digest != null && !digest.isJsonNull() && digest.getAsString().startsWith("sha256:"))
                    ? digest.getAsString().substring("sha256:".length())
                    : null;
            return new Asset(name, URI.create(asset.get("browser_download_url").getAsString()), sha256);
        }
        return null;
    }

    private static Path download(Asset asset) throws Exception {
        Path dir = FabricLoader.getInstance().getConfigDir().resolve(Hook.MOD_ID + "-update");
        Files.createDirectories(dir);
        Path part = dir.resolve(asset.name() + ".part");
        Path pending = dir.resolve(asset.name() + ".pending");

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(asset.url())
                .header("User-Agent", USER_AGENT)
                .timeout(Duration.ofMinutes(5))
                .build();
        HttpResponse<InputStream> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            Hook.LOGGER.error("couldn't download update, github answered with status {}", response.statusCode());
            return null;
        }

        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        try (InputStream in = new DigestInputStream(response.body(), sha256)) {
            Files.copy(in, part, StandardCopyOption.REPLACE_EXISTING);
        }
        String actual = HexFormat.of().formatHex(sha256.digest());
        if (asset.sha256() == null) {
            Hook.LOGGER.warn("github didn't provide a checksum for {}, installing it unverified", asset.name());
        } else if (!asset.sha256().equalsIgnoreCase(actual)) {
            Files.deleteIfExists(part);
            Hook.LOGGER.error("downloaded update is corrupted (sha256 {} instead of {}), not installing it!", actual, asset.sha256());
            return null;
        }
        return Files.move(part, pending, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    // runs in a shutdown hook, so only plain java and System.err here, the logger may already be gone
    private static void install(Path pending, String assetName) {
        try {
            Path current = FabricLoader.getInstance().getModContainer(Hook.MOD_ID).map(ModContainer::getOrigin)
                    .map(origin -> origin.getPaths().getFirst()).orElseThrow();
            Path target = current.resolveSibling(assetName);
            if (!current.equals(target)) Files.delete(current);
            Files.move(pending, target, StandardCopyOption.REPLACE_EXISTING);
            System.err.println("[dchook] installed update " + target.getFileName() + ", it will load on next start");
        } catch (Exception e) {
            System.err.println("[dchook] couldn't install the update automatically (" + e + "), please replace the"
                    + " dchook jar in your mods folder with " + pending + " (renamed to " + assetName + ")");
        }
    }

    private static String localCommit() {
        return FabricLoader.getInstance().getModContainer(Hook.MOD_ID)
                .map(mod -> mod.getMetadata().getCustomValue(Hook.MOD_ID + ":commit"))
                .filter(value -> value.getType() == CustomValue.CvType.STRING)
                .map(CustomValue::getAsString)
                .orElse("dev");
    }

    private static String shortCommit(String commit) {
        return commit.length() > 7 ? commit.substring(0, 7) : commit;
    }
}
