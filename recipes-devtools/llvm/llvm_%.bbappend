# Extend llvm-native with the PE toolchain pieces Wine probes for on ARM64.
LLVM_PROJECTS:class-native = "clang;lld"

EXTRA_OECMAKE:append:class-native = " \
 -DLLVM_ENABLE_PROJECTS='${LLVM_PROJECTS}' \
 -DLLVM_INCLUDE_DOCS=OFF \
 -DLLVM_INCLUDE_TESTS=OFF \
 -DCLANG_INCLUDE_DOCS=OFF \
 -DCLANG_INCLUDE_TESTS=OFF \
 -DCLANG_TOOL_CLANG_FORMAT_BUILD=OFF \
 -DCLANG_TOOL_CLANG_RENAME_BUILD=OFF \
 -DCLANG_TOOL_SCAN_BUILD_BUILD=OFF \
 -DCLANG_TOOL_SCAN_BUILD_PY_BUILD=OFF \
 -DCLANG_TOOL_SCAN_VIEW_BUILD=OFF \
 -DCLANG_INSTALL_SCANBUILD=OFF \
 -DCLANG_INSTALL_SCANVIEW=OFF \
"
LLVM_TARGETS:append:class-native = ";AArch64"

do_install:class-native() {
	if ${@bb.utils.contains('PACKAGECONFIG', 'libllvm', 'true', 'false', d)}; then
		DESTDIR=${D} ninja -v install

		# llvm hardcodes usr/lib as install path, so this corrects it to actual libdir.
		mv -T -n ${D}/${prefix}/lib ${D}/${libdir} || true

		# Keep the native install tree compatible with the upstream OE cleanup logic.
		rm -rf ${D}${datadir}/opt-viewer
		rm -rf ${D}${datadir}/clang
		rmdir ${D}${datadir}

		# Preserve reproducibility without assuming a target-style libdir path layout.
		if [ -d ${D}${libdir}/cmake/llvm ]; then
			find ${D}${libdir}/cmake/llvm -type f -name '*.cmake' \
				-exec sed -i -e 's,${WORKDIR},,g' {} +
		fi
	fi

	create_unversioned_symlink() {
		tool="$1"
		versioned="$2"

		if [ ! -e ${D}${bindir}/$tool ] && [ -e ${D}${bindir}/$versioned ]; then
			ln -sf $versioned ${D}${bindir}/$tool
		fi
	}

	create_unversioned_symlink clang clang-${MAJOR_VERSION}
	create_unversioned_symlink clang++ clang++-${MAJOR_VERSION}
	create_unversioned_symlink ld.lld ld.lld-${MAJOR_VERSION}
	create_unversioned_symlink lld-link lld-link-${MAJOR_VERSION}
}
