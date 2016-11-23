include_defs('//bucklets/gerrit_plugin.bucklet')

gerrit_plugin(
  name = 'server-config',
  srcs = glob(['src/main/java/**/*.java']),
  resources = glob(['src/main/resources/**/*']),
  manifest_entries = [
    'Gerrit-PluginName: server-config',
    'Gerrit-HttpModule: com.googlesource.gerrit.plugins.serverconfig.HttpModule',
  ]
)

# this is required for bucklets/tools/eclipse/project.py to work
# not sure, if this does something useful in standalone context
java_library(
  name = 'classpath',
  deps = [':server-config__plugin'],
)

java_test(
  name = 'server-config_tests',
  srcs = glob(['src/test/java/**/*.java']),
  labels = ['server-config'],
  deps = GERRIT_PLUGIN_API + GERRIT_TESTS + [
    ':server-config__plugin',
    '//lib:junit',
  ],
)
