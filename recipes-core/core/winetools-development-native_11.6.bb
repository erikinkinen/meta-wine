require winetools-native-common.inc

SUMMARY = "Wine development tools for native builds"
WINEGCC_PATCH = "file://patch_winegcc_11.6.patch"
WINE_TOOLS_SUBDIR = "wine-development-tools"

SRC_URI[wine.sha256sum] = "d49d166975478f609e6a9cdbda0a07c65a3b795e061fc454d3f1034c828d19e0"
