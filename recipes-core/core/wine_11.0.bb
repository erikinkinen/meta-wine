SUMMARY = "Wine is a compatibility layer for running Windows applications on Unix-like operating systems."
DESCRIPTION = "Wine is a compatibility layer for running Windows applications on Unix-like operating systems. It provides a software library, known as Winelib, against which developers can compile Windows applications to help port them to Unix-like systems. Wine also provides a program loader, which allows many unmodified Windows applications to run on Unix-like systems."
HOMEPAGE = "https://www.winehq.org/"
SECTION = "devel"

inherit autotools mime-xdg pkgconfig

AUTORECONF = "no"
LICENSE = "LGPL-2.1-or-later"

SRC_URI = "https://dl.winehq.org/wine/source/${PV}/wine-${PV}.tar.xz;subdir=${BP};striplevel=1 \
           file://patch_winegcc.patch \
           file://patch_preloader_ldflags.patch \
"
SRC_URI[sha256sum] = "c07a6857933c1fc60dff5448d79f39c92481c1e9db5aa628db9d0358446e0701"

S = "${WORKDIR}/${BP}"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=727e499c6b317fcc90840ad84415793f"

DEPENDS += "autoconf automake libtool pkgconfig \
           alsa-lib \
           fontconfig \
           freetype \
           libusb1 \
           pulseaudio \
           udev \
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

EXTRA_OECONF += " --with-wine-tools=${STAGING_BINDIR_NATIVE}"
EXTRA_OECONF += "${@bb.utils.contains('DISTRO_FEATURES', 'x11', '', '--without-x', d)}"
EXTRA_OEMAKE += "WITH_NLS=./nls"

DEPENDS += "winetools-native flex-native bison-native"

FILES:${PN}-staticdev += "${libdir}/**/*.a ${includedir}"
FILES:${PN}-dev += "${libdir}/*.so ${includedir}"
