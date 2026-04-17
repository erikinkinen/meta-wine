require wine-common.inc

inherit python3native

SUMMARY = "Wine staging compatibility layer"
WINEGCC_PATCH = "file://patch_winegcc_11.6.patch"
WINE_TOOLS_RECIPE = "winetools-development-native"
WINE_TOOLS_SUBDIR = "wine-development-tools"
WINE_LICENSE_MD5 = "6fea1fffb53725359ad99ea792f90aac"

SRC_URI += "https://gitlab.winehq.org/wine/wine-staging/-/archive/v${PV}/wine-staging-v${PV}.tar.gz;name=staging"
SRC_URI:append:aarch64 = " \
           file://0001-dcomp-tests-add-aarch64-stack-pointer-support.patch \
           file://0002-makedep-restore-target-flags-for-winegcc.patch \
"

SRC_URI[wine.sha256sum] = "d49d166975478f609e6a9cdbda0a07c65a3b795e061fc454d3f1034c828d19e0"
SRC_URI[staging.sha256sum] = "870217ad302d289e776f6d259790900425d8a6a8e72ad88276f344c27ddb992f"

do_patch[depends] += "python3-native:do_populate_sysroot"

wine_staging_apply_patches() {
        DESTDIR="${S}" ${PYTHON} ${WORKDIR}/wine-staging-v${PV}/staging/patchinstall.py --all --backend=patch --no-autoconf
}

do_patch[prefuncs] += "wine_staging_apply_patches"

wine_staging_regenerate_build_system() {
        cd ${S}
        autoreconf -f
        ./tools/make_requests
}

do_configure[prefuncs] += "wine_staging_regenerate_build_system"
