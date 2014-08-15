@PLUGIN@ - /server-config/ REST API
===================================

This page describes the REST endpoints that are added by the @PLUGIN@
plugin.

Please also take note of the general information on the
[REST API](../../../Documentation/rest-api.html).

<a id="server-config-endpoints"> Server Config Endpoints
------------------------------------------

Get the content of a configuration file.

### <a id="get-content"> Read File
_GET /plugins/server-config/\{path\}_

Read the content of a configuration file.

#### Request

```
  GET /plugins/server-config/etc/gerrit.config HTTP/1.0
```

As response the content of the configuration file gerrit.config is returned.
Only Gerrit administrators are allowed to read the content of a configuration
file.

#### Response

```
  HTTP/1.1 200 OK
  Content-Type: application/octet-stream

  [gerrit]
    basePath = git
    canonicalWebUrl = ...
    ....
```

### <a id="write-content"> Write File
_PUT /plugins/server-config/\{path\}_

Update the content of a configuration file. Only Gerrit administrators are
allowed to read the content of a configuration file. The update does not get
into effect immediately. A restart may be required.


#### Request

```
  PUT /plugins/server-config/etc/gerrit.config HTTP/1.0

  [gerrit]
    basePath = git
    canonicalWebUrl = ...
    ....
```

#### Response
```
  HTTP/1.1 204 No Content
```

