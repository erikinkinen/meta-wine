SUMMARY ?= "Wine tools for native builds"
DESCRIPTION = "Wine tools for native builds"
HOMEPAGE = "https://www.winehq.org/"
SECTION = "devel"

inherit autotools pkgconfig
inherit native

AUTORECONF = "no"
LICENSE = "LGPL-2.1-or-later"

WINE_SOURCE_SERIES ?= "${@d.getVar('PV').split('.')[0]}.x"
WINE_TARBALL_URI ?= "https://dl.winehq.org/wine/source/${WINE_SOURCE_SERIES}/wine-${PV}.tar.xz"
WINEGCC_PATCH ?= "file://patch_winegcc.patch"
WINE_TOOLS_SUBDIR ?= ""

SRC_URI = "${WINE_TARBALL_URI};name=wine;subdir=${BP};striplevel=1 \
           ${WINEGCC_PATCH} \
"

S = "${WORKDIR}/${BP}"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=727e499c6b317fcc90840ad84415793f"

DEPENDS = "autoconf automake libtool pkgconfig freetype-native"
DEPENDS += "flex-native bison-native"

AUTOTOOLS_SCRIPT_PATH ?= "${S}"
CONFIGURE_SCRIPT ?= "${AUTOTOOLS_SCRIPT_PATH}/configure"

do_configure() {
        if [ -e ${CONFIGURE_SCRIPT} ]; then
                oe_runconf
        else
                bbnote "nothing to configure"
        fi
}

autotools_aclocals() {
}

BBCLASSEXTEND = "native nativesdk"

EXTRA_OECONF += " --without-x "

python __anonymous() {
    import re
    host_arch = d.getVar('HOST_ARCH')
    if re.match(r'x86_64|amd64', host_arch):
        d.appendVar('EXTRA_OECONF', ' --enable-win64 ')
}

do_install() {
        install -d ${D}${bindir}/${WINE_TOOLS_SUBDIR}
        install -d ${D}${bindir}/${WINE_TOOLS_SUBDIR}/nls
        cp -R ${WORKDIR}/build/tools ${D}${bindir}/${WINE_TOOLS_SUBDIR}
        cp -R ${S}/nls/*.nls ${D}${bindir}/${WINE_TOOLS_SUBDIR}/nls
}
