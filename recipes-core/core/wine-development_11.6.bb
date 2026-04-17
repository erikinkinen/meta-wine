require wine-common.inc

SUMMARY = "Wine development compatibility layer"
WINEGCC_PATCH = "file://patch_winegcc_11.6.patch"
WINE_TOOLS_RECIPE = "winetools-development-native"
WINE_TOOLS_SUBDIR = "wine-development-tools"
WINE_LICENSE_MD5 = "6fea1fffb53725359ad99ea792f90aac"

SRC_URI:append:aarch64 = " \
           file://0001-dcomp-tests-add-aarch64-stack-pointer-support.patch \
           file://0002-makedep-restore-target-flags-for-winegcc.patch \
"

SRC_URI[wine.sha256sum] = "d49d166975478f609e6a9cdbda0a07c65a3b795e061fc454d3f1034c828d19e0"
