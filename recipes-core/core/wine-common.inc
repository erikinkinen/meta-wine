SUMMARY ?= "Wine is a compatibility layer for running Windows applications on Unix-like operating systems."
DESCRIPTION = "Wine is a compatibility layer for running Windows applications on Unix-like operating systems. It provides a software library, known as Winelib, against which developers can compile Windows applications to help port them to Unix-like systems. Wine also provides a program loader, which allows many unmodified Windows applications to run on Unix-like systems."
HOMEPAGE = "https://www.winehq.org/"
SECTION = "devel"

inherit autotools mime-xdg pkgconfig

AUTORECONF = "no"
LICENSE = "LGPL-2.1-or-later"

WINE_SOURCE_SERIES ?= "${@d.getVar('PV').split('.')[0]}.x"
WINE_TARBALL_URI ?= "https://dl.winehq.org/wine/source/${WINE_SOURCE_SERIES}/wine-${PV}.tar.xz"
WINEGCC_PATCH ?= "file://patch_winegcc.patch"
WINE_TOOLS_RECIPE ?= ""
WINE_TOOLS_SUBDIR ?= ""
WINE_LICENSE_MD5 ?= "727e499c6b317fcc90840ad84415793f"

SRC_URI = "${WINE_TARBALL_URI};name=wine;subdir=${BP};striplevel=1 \
           ${WINEGCC_PATCH} \
           file://patch_preloader_ldflags.patch \
"

S = "${WORKDIR}/${BP}"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=${WINE_LICENSE_MD5}"

RPROVIDES:${PN} += "virtual-wine"

DEPENDS += "autoconf automake libtool pkgconfig \
           alsa-lib \
           fontconfig \
           freetype \
           libusb1 \
           pulseaudio \
           udev \
           ${WINE_TOOLS_RECIPE} \
           flex-native \
           bison-native \
"
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'x11', 'libx11 libxcomposite libxcursor libxi libxinerama libxrandr', '', d)}"
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'x11 opengl', 'libxxf86vm virtual/libgl', '', d)}"
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'wayland', 'libxkbcommon wayland wayland-native wayland-protocols', '', d)}"
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'opengl', 'virtual/egl', '', d)}"
DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'vulkan', 'vulkan-loader', '', d)}"

AUTOTOOLS_SCRIPT_PATH ?= "${S}"
CONFIGURE_SCRIPT ?= "${AUTOTOOLS_SCRIPT_PATH}/configure"

do_configure:prepend() {
        # Wine's configure only looks for a host-prefixed pkg-config binary.
        install -d ${B}/hosttools
        ln -sf ${STAGING_BINDIR_NATIVE}/pkg-config ${B}/hosttools/${HOST_PREFIX}pkg-config
        export PATH="${B}/hosttools:${PATH}"
}

do_configure() {
        if [ -e ${CONFIGURE_SCRIPT} ]; then
                oe_runconf
        else
                bbnote "nothing to configure"
        fi
}

autotools_aclocals() {
}

do_install:append() {
        install -d ${D}${bindir}

        wine_loader=$(find ${D}${libdir}/wine -maxdepth 2 -type f -name wine | sed -n '1p')
        if [ -z "${wine_loader}" ]; then
                bbfatal "Could not find installed Wine loader under ${D}${libdir}/wine"
        fi

        wine_loader_dir=$(basename "$(dirname "${wine_loader}")")
        ln -snf ../lib/wine/${wine_loader_dir}/wine ${D}${bindir}/wine
}

EXTRA_OECONF += " --with-wine-tools=${STAGING_BINDIR_NATIVE}/${WINE_TOOLS_SUBDIR}"
EXTRA_OECONF += "${@bb.utils.contains('DISTRO_FEATURES', 'x11', '', '--without-x', d)}"
EXTRA_OEMAKE += "WITH_NLS=./nls"

FILES:${PN}-staticdev += "${libdir}/**/*.a ${includedir}"
FILES:${PN}-dev += "${libdir}/*.so ${includedir}"
FILES:${PN} += "${libdir}/wine ${datadir}/wine"
