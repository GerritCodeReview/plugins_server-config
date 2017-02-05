load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "gerrit_plugin",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
)

gerrit_plugin(
    name = "server-config",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**/*"]),
    manifest_entries = [
        "Gerrit-PluginName: server-config",
        "Gerrit-HttpModule: com.googlesource.gerrit.plugins.serverconfig.HttpModule",
    ],
)

junit_tests(
    name = "server_config_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    tags = ["server-config"],
    deps = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
        ":server-config__plugin",
    ],
)