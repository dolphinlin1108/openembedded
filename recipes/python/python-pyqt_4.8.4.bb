DESCRIPTION = "Python Qt4 Bindings"
HOMEPAGE = "http://riverbankcomputing.co.uk"
AUTHOR = "Phil Thomson @ riverbank.co.uk"
SECTION = "devel/python"
PRIORITY = "optional"
LICENSE = "GPL"
DEPENDS = "sip-native python-sip"
RDEPENDS_${PN} = "python-core"
SRCNAME = "pyqt"
PR = "r0"

PYQT_OE_VERSION = "Qt_4_7_1"

SRC_URI = "\
  http://www.riverbankcomputing.co.uk/static/Downloads/PyQt4/PyQt-x11-gpl-${PV}.tar.gz \
  \
  file://fix_qthelp_ftbfs.diff;apply=yes \
  file://fix_the_QAssitant_ftbfs.diff;apply=yes \
  file://assistantclient-fix.patch;apply=yes \
"
SRC_URI[md5sum] = "97c5dc1042feb5b3fe20baabad055af1"
SRC_URI[sha256sum] = "fcfa3ecc0b4fad6d93227751b36a6f81ea104ee19dd26905f52de59f060b3e98"
S = "${WORKDIR}/PyQt-x11-gpl-${PV}"

# arm and mips machines need some extra patches
SRC_URI_append_arm = "\
  file://qreal_float_support.diff;apply=yes \
"

SRC_URI_append_mipsel = "\
  file://qreal_float_support.diff;apply=yes \
"

inherit qt4x11 sip distutils-base

PARALLEL_MAKE = ""

QMAKE_PROFILES = "pyqt.pro"
# NOTE: has to match with MIN(qt version we have in OE, last known Qt version by SIP/PyQt)
EXTRA_SIPTAGS = "-tWS_X11 -t${PYQT_OE_VERSION} -xVendorID -xPyQt_SessionManager -xPyQt_Accessibility"
EXTRA_OEMAKE = " MAKEFLAGS= "

# arm and mips need extra params for the qreal issue
EXTRA_SIPTAGS_append_arm = " -x PyQt_qreal_double"
EXTRA_SIPTAGS_append_mipsel = " -x PyQt_qreal_double" 

SIP_MODULES = "QtCore QtDeclarative QtGui QtNetwork QtSql QtSvg QtXml QtWebKit"
MAKE_MODULES = "qpy ${SIP_MODULES}"

EXTRA_QMAKEVARS_POST += "\
  INCLUDEPATH+=${OE_QMAKE_INCDIR_QT}/Qt \
  INCLUDEPATH+=${STAGING_INCDIR}/${PYTHON_DIR} \
  INCLUDEPATH+=../qpy/QtCore \
  INCLUDEPATH+=../qpy/QtGui \
  INCLUDEPATH+=../qpy/QtDeclarative \
  INCLUDEPATH+=${OE_QMAKE_INCDIR_QT}/QtCore \
  INCLUDEPATH+=${OE_QMAKE_INCDIR_QT}/QtGui \
  INCLUDEPATH+=${OE_QMAKE_INCDIR_QT}/QtDeclarative \
  INCLUDEPATH+=${OE_QMAKE_INCDIR_QT}/QtWebKit \
  INCLUDEPATH+=${OE_QMAKE_INCDIR_QT}/QtNetwork \
"
FIX_QREAL = "\
"

do_generate_prepend() {
    for i in ${FIX_QREAL}; do
        sed -i -e s,qreal,float,g sip/$i
    done
}

do_configure_prepend() {
    printf "TEMPLATE=subdirs\nSUBDIRS=${MAKE_MODULES}\n" >pyqt.pro
    printf "TEMPLATE=subdirs\nSUBDIRS=QtCore QtDeclarative QtGui\n" >qpy/qpy.pro
    ln -sf ./qpycore.pro qpy/QtCore/QtCore.pro
    ln -sf ./qpydeclarative.pro qpy/QtDeclarative/QtDeclarative.pro
    ln -sf ./qpygui.pro qpy/QtGui/QtGui.pro
    echo "INCLUDEPATH+=${S}/QtCore" >>qpy/QtCore/QtCore.pro
    echo "INCLUDEPATH+=${S}/QtGui" >>qpy/QtGui/QtGui.pro
    echo "INCLUDEPATH+=${S}/QtDeclarative" >>qpy/QtDeclarative/QtDeclarative.pro
    echo "LIBS+=-L../qpy/QtGui/ -lqpygui" >>QtGui/QtGui.pro
    echo "LIBS+=-L../qpy/QtCore/ -lqpycore" >>QtCore/QtCore.pro
    echo "LIBS+=-L../qpy/QtDeclarative/ -lqpydeclarative" >>QtDeclarative/QtDeclarative.pro
}

do_stage() {
    install -d ${STAGING_SIPDIR}/qt/
    install -d ${STAGING_LIBDIR}/${PYTHON_DIR}/site-packages
    for module in ${SIP_MODULES}
    do
        install -m 0644 ${S}/sip/${module}/*.sip ${STAGING_SIPDIR}/qt/
	install -m 0755 ${module}/lib${module}.so ${STAGING_LIBDIR}/${PYTHON_DIR}/site-packages/${module}.so
    done
}

do_install() {
    install -d ${D}${libdir}/${PYTHON_DIR}/site-packages/PyQt4
    for module in ${SIP_MODULES}
    do
		echo "from PyQt4.${module} import *\n" >> ${D}${libdir}/${PYTHON_DIR}/site-packages/PyQt4/Qt.py
		install -m 0755 ${module}/lib${module}.so ${D}${libdir}/${PYTHON_DIR}/site-packages/PyQt4/${module}.so
    done
	cp -pPR elementtree ${D}${libdir}/${PYTHON_DIR}/site-packages/PyQt4/
	cp __init__.py ${D}${libdir}/${PYTHON_DIR}/site-packages/PyQt4/
}

FILES_${PN} = "${libdir}/${PYTHON_DIR}/site-packages"

