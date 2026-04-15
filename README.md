# meta-wine

`meta-wine` is an OpenEmbedded/Yocto layer that packages the Wine Windows
compatibility platform for target images.

The layer currently supports the `scarthgap` release series and provides
multiple Wine variants so images can choose between a conservative stable
release, an upstream development snapshot, or the Wine Staging patchset.

## What This Layer Provides

Target packages:

- `wine-stable` (`11.0`)
- `wine-development` (`11.6`)
- `wine-staging` (`11.6`)
- `virtual-wine` runtime provider implemented by all target Wine variants

Native build helpers used internally by the target recipes:

- `winetools-stable-native` (`11.0`)
- `winetools-development-native` (`11.6`)

All target variants install a `/usr/bin/wine` launcher symlink in addition to
the versioned runtime layout under `/usr/lib/wine` and `/usr/share/wine`.

## Variant Selection

The layer does not expose a target recipe named `wine`. The historical `wine`
package name has been split into explicit variants:

- `wine-stable`: stable Wine release, suitable as the default choice
- `wine-development`: upstream development release
- `wine-staging`: upstream development release plus the Wine Staging patchset

All three target packages advertise the `virtual-wine` runtime package. The
layer default is configured in [conf/layer.conf](conf/layer.conf):

```conf
PREFERRED_RPROVIDER_virtual-wine ?= "wine-stable"
```

That gives you two supported ways to consume Wine:

1. Install a concrete package such as `wine-staging` for a fixed variant.
2. Install `virtual-wine` and override `PREFERRED_RPROVIDER_virtual-wine` when
   you want the image or distro configuration to choose the implementation.

Only install one Wine target variant in an image at a time. The variants ship
the same runtime paths and are intended to be alternatives, not co-installed
packages.

## Adding The Layer

Add `meta-wine` to `BBLAYERS`:

```conf
BBLAYERS += "/path/to/meta-wine"
```

The layer declares:

- `LAYERDEPENDS_meta-wine = "core"`
- `LAYERSERIES_COMPAT_meta-wine = "scarthgap"`

## Image Integration

Install a concrete Wine variant:

```conf
IMAGE_INSTALL:append = " wine-staging"
```

Or consume the runtime virtual package and select the provider separately:

```conf
PREFERRED_RPROVIDER_virtual-wine = "wine-development"
IMAGE_INSTALL:append = " virtual-wine"
```

## Feature Notes

The shared Wine recipe logic lives in
[recipes-core/core/wine-common.inc](recipes-core/core/wine-common.inc), with
variant-specific recipes selecting the Wine source version and toolchain helper
recipe. The recipes adapt to enabled distro features:

- `x11` enables X11 support
- `wayland` enables Wayland support
- `opengl` enables OpenGL and EGL integration where applicable
- `vulkan` enables Vulkan loader support

When `x11` is not enabled, the recipes configure Wine with `--without-x`.

`wine-staging` applies the Wine Staging patchset during `do_patch` and
regenerates the build system before `do_configure`.

## Source Layout

Important files in the layer:

- [conf/layer.conf](conf/layer.conf): layer registration and default
  `virtual-wine` provider
- [recipes-core/core/wine-common.inc](recipes-core/core/wine-common.inc):
  shared target recipe logic
- [recipes-core/core/wine-stable_11.0.bb](recipes-core/core/wine-stable_11.0.bb):
  stable target recipe
- [recipes-core/core/wine-development_11.6.bb](recipes-core/core/wine-development_11.6.bb):
  development target recipe
- [recipes-core/core/wine-staging_11.6.bb](recipes-core/core/wine-staging_11.6.bb):
  staging target recipe
- [recipes-core/core/winetools-native-common.inc](recipes-core/core/winetools-native-common.inc):
  shared native helper recipe logic
- [recipes-core/core/winetools-stable-native_11.0.bb](recipes-core/core/winetools-stable-native_11.0.bb):
  stable native helper
- [recipes-core/core/winetools-development-native_11.6.bb](recipes-core/core/winetools-development-native_11.6.bb):
  development/staging native helper
- [recipes-core/core/files/patch_preloader_ldflags.patch](recipes-core/core/files/patch_preloader_ldflags.patch):
  linker flag fix for the Wine preloader
- [recipes-core/core/files/patch_winegcc.patch](recipes-core/core/files/patch_winegcc.patch):
  Wine `11.0` build fix
- [recipes-core/core/files/patch_winegcc_11.6.patch](recipes-core/core/files/patch_winegcc_11.6.patch):
  Wine `11.6` build fix

## Status

The layer is under active development, but the current recipe set has been
validated to build and run on `scarthgap` with the `x11`, `opengl`, and `vulkan` features enabled.
