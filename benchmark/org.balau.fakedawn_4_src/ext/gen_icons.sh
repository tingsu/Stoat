#!/bin/sh -e

#   Copyright 2012 Francesco Balducci
#
#   This file is part of FakeDawn.
#
#   FakeDawn is free software: you can redistribute it and/or modify
#   it under the terms of the GNU General Public License as published by
#   the Free Software Foundation, either version 3 of the License, or
#   (at your option) any later version.
#
#   FakeDawn is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU General Public License for more details.
#
#   You should have received a copy of the GNU General Public License
#   along with FakeDawn.  If not, see <http://www.gnu.org/licenses/>.

# Use ImageMagick to convert svg into png icons.
# Needs both ImageMagick and libsvg2-bin

convert ./FakeDawn.svg -adaptive-resize 96x96 ../res/drawable-xhdpi/ic_launcher.png
convert ./FakeDawn.svg -adaptive-resize 72x72 ../res/drawable-hdpi/ic_launcher.png
convert ./FakeDawn.svg -adaptive-resize 48x48 ../res/drawable-mdpi/ic_launcher.png
convert ./FakeDawn.svg -adaptive-resize 36x36 ../res/drawable-ldpi/ic_launcher.png

convert ./sound.svg -adaptive-resize 96x96 ../res/drawable/sound.png

convert ./sunrise.svg -adaptive-resize 96x96 ../res/drawable/sunrise.png

