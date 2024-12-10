# Magnolia Vanity-URL App

[![build-module](https://github.com/IBM/magkit-vanity-url/actions/workflows/build.yaml/badge.svg)](https://github.com/IBM/magkit-vanity-url/actions/workflows/build.yaml)
[![Magnolia compatibility](https://img.shields.io/badge/magnolia-6.2-brightgreen.svg)](https://www.magnolia-cms.com)
[![Magnolia compatibility](https://img.shields.io/badge/magnolia-6.3-brightgreen.svg)](https://www.magnolia-cms.com)

## Scope

A [module](https://documentation.magnolia-cms.com/display/DOCS/Modules) containing an [app](https://documentation.magnolia-cms.com/display/DOCS/Apps) for the [Magnolia CMS](http://www.magnolia-cms.com)

Allows to configure vanity URLs in the Magnolia CMS without requiring access to the config workspace. Ideal for page/content editors who are not supposed to write to the config workspace. Also creates QR codes for quick testing with your mobile phone.

### Maven artifacts in Magnolia's Nexus

The code is built by [GitHub actions](https://github.com/IBM/magkit-vanity-url/actions/workflows/build.yaml).
You can browse available artifacts through [Magnolia's Nexus](https://nexus.magnolia-cms.com/#browse/search=name.raw%3Dmagkit-vanity-url)

### Maven dependency

```xml
    <dependency>
        <artifactId>magkit-vanity-url</artifactId>
        <groupId>de.ibmix.magkit</groupId>
        <version>1.7.0</version>
    </dependency>
```

#### Versions

* Version 1.7.x is compatible with Magnolia 6.3.x (new UI support again)
* Version 1.6.x is compatible with Magnolia 6.2.x (new UI support)

The vanity url module was migrated to new Maven coordinates. For older Magnolia versions you have to use the old Maven coordinates like: `com.aperto.magkit:magnolia-vanity-url:1.5.4`

* Version 1.5.x is compatible with Magnolia 5.6.x, 5.7.x, 6.1.x and 6.2.x
* Version 1.4.x is compatible with Magnolia 5.4.x and 5.5.x
* Version 1.3.x is compatible with Magnolia 5.3.x
* Version 1.2.x is compatible with Magnolia 5.2.x

See also the [change log](CHANGELOG.md) and [security advices](SECURITY.md)

### Magnolia Module Configuration

You can configure the following settings in the module configuration by JCR or yaml:
* _excludes_ : Pattern of urls, which are no candidates for vanity urls.
  * by default an exclude for all urls containing a dot is configured, that prevents the virtual uri mapping checks every ordinary request like script.js or page.html 
* _publicUrlService_ : Implementation of _de.ibmix.magkit.vanityurl.PublicUrlService_. Two implementations are already available.
  * _de.ibmix.magkit.vanityurl.DefaultPublicUrlService_ (default) : Use of default base url and site configuration with context path replacement.
  * _de.ibmix.magkit.vanityurl.SimplePublicUrlService_ : Uses configured public prefix and removes the author context path.
* _headlessEndpoint_ : is used for headless support and defines the pages rest endpoint which should be checked for vanity urls
* _previewImage_ : image type of preview image. SVG and PNG is possible (default is SVG).

#### Headless support (with version 1.6.1)

For using the headless support you have to decorate the default vanity url virtual uri mapping. It is also possible to define an additional virtual uri mapping 
if you want to support both renderings. All you need is the following yaml which you can place in your module as decoration of the vanity url module 
(your-module/decorations/magkit-vanity-url/virtualUriMappings) or as own virtual uri mapping (your-module/virtualUriMappings).
```yaml
class: de.ibmix.magkit.vanityurl.HeadlessVirtualVanityUriMapping
```

## License

Apache 2.0 license is used. If you would like to see the detailed LICENSE click [here](LICENSE).

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

See also [CONTRIBUTING.md](CONTRIBUTING.md) and [MAINTAINERS.md](MAINTAINERS.md)
