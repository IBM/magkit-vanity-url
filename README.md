# Magnolia Vanity-URL App

[![build-module](https://github.com/IBM/magkit-vanity-url/actions/workflows/build.yaml/badge.svg)](https://github.com/IBM/magkit-vanity-url/actions/workflows/build.yaml) 
[![Magnolia compatibility](https://img.shields.io/badge/magnolia-5.4-brightgreen.svg)](https://www.magnolia-cms.com)
[![Magnolia compatibility](https://img.shields.io/badge/magnolia-5.5-brightgreen.svg)](https://www.magnolia-cms.com)
[![Magnolia compatibility](https://img.shields.io/badge/magnolia-5.6-brightgreen.svg)](https://www.magnolia-cms.com)
[![Magnolia compatibility](https://img.shields.io/badge/magnolia-5.7-brightgreen.svg)](https://www.magnolia-cms.com)
[![Magnolia compatibility](https://img.shields.io/badge/magnolia-6.1-brightgreen.svg)](https://www.magnolia-cms.com)
[![Magnolia compatibility](https://img.shields.io/badge/magnolia-6.2-brightgreen.svg)](https://www.magnolia-cms.com)

## Scope

A [module](https://documentation.magnolia-cms.com/display/DOCS/Modules) containing an [app](https://documentation.magnolia-cms.com/display/DOCS/Apps) for the [Magnolia CMS](http://www.magnolia-cms.com)

Allows to configure vanity URLs in the Magnolia CMS without requiring access to the config workspace. Ideal for page/content editors who are not supposed to write to the config workspace. Also creates QR codes for quick testing with your mobile phone.

## Usage

This repository contains some example best practices for open source repositories:

* [LICENSE](LICENSE)
* [README.md](README.md)
* [CONTRIBUTING.md](CONTRIBUTING.md)
* [MAINTAINERS.md](MAINTAINERS.md)
<!-- A Changelog allows you to track major changes and things that happen, https://github.com/github-changelog-generator/github-changelog-generator can help automate the process -->
* [CHANGELOG.md](CHANGELOG.md)

### Issue tracking

Issues are tracked at [GitHub](https://github.com/IBM/magkit-vanity-url/issues).

Any bug reports, improvement or feature pull requests are very welcome! 
Make sure your patches are well tested. Ideally create a topic branch for every separate change you make. 
For example:

1. Fork the repo
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Added some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

### Maven artifacts in Magnolia's Nexus

The code is built on [Travis CI](https://app.travis-ci.com/github/IBM/magkit-vanity-url).
You can browse available artifacts through [Magnolia's Nexus](https://nexus.magnolia-cms.com/#nexus-search;quick~magnolia-vanity-url)

### Maven dependency

```xml
    <dependency>
        <artifactId>magnolia-vanity-url</artifactId>
        <groupId>com.aperto.magkit</groupId>
        <version>1.6.0</version>
    </dependency>
```

### Versions
-----------------
* Version 1.2.x is compatible with Magnolia 5.2.x
* Version 1.3.x is compatible with Magnolia 5.3.x
* Version 1.4.x is compatible with Magnolia 5.4.x and 5.5.x
* Version 1.5.x is compatible with Magnolia 5.6.x, 5.7.x, 6.1.x and 6.2.x
* Version 1.6.x is compatible with Magnolia 6.2.x (new UI support)

### Magnolia Module Configuration

In the module configuration of the vanity url module, you can configure the following settings:
* _excludes_ : Pattern of urls, which are no candidates for vanity urls.
  * by default an exclude for all urls containing a dot is configured, that prevents the virtual uri mapping checks every ordinary request like script.js or page.html 
* _publicUrlService_ : Implementation of _com.aperto.magnolia.vanity.PublicUrlService_. Two implementations are already available.
  * _com.aperto.magnolia.vanity.DefaultPublicUrlService_ (default) : Use of default base url and site configuration with context path replacement.
  * _com.aperto.magnolia.vanity.SimplePublicUrlService_ : Used configured public prefix and removes the author context path.

## License

All source files must include a Copyright and License header. The SPDX license header is
preferred because it can be easily scanned.

If you would like to see the detailed LICENSE click [here](LICENSE).

```text
#
# Copyright 2020- IBM Inc. All rights reserved
# SPDX-License-Identifier: Apache2.0
#
```
## Authors

Optionally, you may include a list of authors, though this is redundant with the built-in
GitHub list of contributors.

- Author: Frank Sommer <frank.sommer1@ibm.com>
