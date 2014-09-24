include_defs('//lib/maven.defs')

API_VERSION = '2.9-SNAPSHOT'
REPO = MAVEN_LOCAL

gerrit_plugin(
  name = 'server-config',
  srcs = glob(['src/main/java/**/*.java']),
  resources = glob(['src/main/resources/**/*']),
  manifest_entries = [
    'Gerrit-PluginName: server-config',
    'Gerrit-HttpModule: com.googlesource.gerrit.plugins.serverconfig.HttpModule',
  ]
)

java_test(
  name = 'server-config_tests',
  srcs = glob(['src/test/java/**/*.java']),
  labels = ['server-config-plugin'],
  deps = [
    ':server-config__plugin',
    '//lib/jgit:jgit',
    '//lib:junit',
  ],
  source_under_test = [':server-config__plugin'],
)
