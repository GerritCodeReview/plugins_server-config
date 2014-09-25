Build
=====

This plugin can be built with Buck or Maven.

Buck
----

Two build modes are supported: Standalone and in Gerrit tree.
The in-tree build mode is recommended if this plugin shall be
build together with the Gerrit tree.
The standalone build mode can be use to build this plugin
independently of the Gerrit tree.

### Build standalone

Clone bucklets library:

```
  git clone https://gerrit.googlesource.com/bucklets

```
and link it to quota plugin directory:

```
  cd quota && ln -s ../bucklets .
```

Add link to the .buckversion file:

```
  cd quota && ln -s bucklets/buckversion .buckversion
```

To build the plugin, issue the following command:


```
  buck build plugin
```

The output is created in

```
  buck-out/gen/quota.jar
```


### Build in Gerrit tree

Clone or link this plugin to the plugins directory of Gerrit's source
tree, and issue the command:

```
  buck build plugins/quota
```

The output is created in

```
  buck-out/gen/plugins/quota/quota.jar
```

This project can be imported into the Eclipse IDE:

```
  ./tools/eclipse/project.py
```

Maven
-----

Note that the Maven build is provided for compatibility reasons, but
it is considered to be deprecated and will be removed in a future
version of this plugin.

To build with Maven, run

```
mvn clean package
```
