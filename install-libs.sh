#!/usr/bin/env bash
# ============================================================
#  MossLuckyTuccar - Yerel kutuphane kurulumu (Linux/macOS)
# ============================================================
#  ShopGUI+ API jar'i genel Maven depolarindan inmez.
#  Bu script onu projedeki lib/ klasorunden yerel Maven
#  deposuna (.m2) kurar. Internet GEREKTIRMEZ.
#
#  Projeyi ilk kez derlemeden once BIR KEZ calistirin:
#      ./install-libs.sh
#  Ardindan normal sekilde derleyin:
#      mvn clean package
# ============================================================
set -e
cd "$(dirname "$0")"

echo "ShopGUI+ API yerel Maven deposuna kuruluyor..."
mvn install:install-file \
  -Dfile="lib/ShopGUIPlus-1.113.0.jar" \
  -DgroupId=net.brcdev \
  -DartifactId=shopgui \
  -Dversion=1.113.0 \
  -Dpackaging=jar

echo ""
echo "Bitti. Artik 'mvn clean package' calistirabilirsiniz."
