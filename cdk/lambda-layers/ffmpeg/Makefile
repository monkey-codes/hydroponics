clean: 
	rm -rf build
	rm -rf bin

bin: 
	mkdir -p build
	mkdir -p bin
	rm -rf build/ffmpeg*
	cd build && curl https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-amd64-static.tar.xz | tar x
	mv build/ffmpeg*/ffmpeg build/ffmpeg*/ffprobe bin
	rm -rf build
