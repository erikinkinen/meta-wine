SUMMARY = "Wine tools for native builds"
DESCRIPTION = "Wine tools for native builds"
HOMEPAGE = "https://www.winehq.org/"
SECTION = "devel"

inherit autotools pkgconfig
inherit native

AUTORECONF = "no"
LICENSE = "LGPL-2.1-or-later"

# The winegcc patch fixes how winegcc calls the cross compiler
# It does this by removing the -b switch from calls to winegcc
# Making winegcc fall back on the default which is the one the CC environment variable points to
# during the configure stage. Since bitbake adds all the necessary switches for the cross compiler to
# find libs to CC this fixes the build w.r.t winegcc
SRC_URI = "https://dl.winehq.org/wine/source/${PV}/wine-${PV}.tar.xz;subdir=${BP};striplevel=1 \
           file://patch_winegcc.patch \
"
SRC_URI[sha256sum] = "c07a6857933c1fc60dff5448d79f39c92481c1e9db5aa628db9d0358446e0701"

S = "${WORKDIR}/${BP}"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=727e499c6b317fcc90840ad84415793f"

DEPENDS = "autoconf automake libtool pkgconfig freetype-native"

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

DEPENDS += "flex-native bison-native"

EXTRA_OECONF += " --without-x "

# Conditionally add --enable-win64 based on native architecture
# This is needed to make sure the configure script won't tell gcc to generate 32 bit applications
# during the configure stage and mess up any executables being built and needed during compilation
# such as makedep
# Still need to figure out what the effect is on the winetools and if they behave differently when
# build for 32 bit or 64 bit or arm or x86(32/64)

python __anonymous() {
    import re
    host_arch = d.getVar('HOST_ARCH')
    if re.match(r'x86_64|amd64', host_arch):
        d.appendVar('EXTRA_OECONF', ' --enable-win64 ')
}

do_install() {
	install -d ${D}${bindir}
	install -d ${D}${bindir}/nls
	cp -R ${WORKDIR}/build/tools ${D}${bindir}
	cp -R ${S}/nls/*.nls ${D}${bindir}/nls
}
