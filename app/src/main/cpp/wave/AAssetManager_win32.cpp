#if WIN32
#include "AAssetManager_win32.h"
#include <cstring>

AAsset::AAsset(std::string source) {
	filePath = source;
}

AAsset* AAssetManager_open(AAssetManager* mgr, std::string filename, ASSET_STREAM mode) {
	char *buffer = new char[256];
	snprintf(buffer, 256, "%s\\%s", mgr->dir.c_str(), filename.c_str());
	std::string name = buffer;

	return new AAsset(name);
}

int AAsset_getLength(AAsset* asset) {
	std::ifstream nstr(asset->filePath);
	std::string content;
	std::string line = "";

	while (!nstr.eof()) {
		std::getline(nstr, line);
		content.append(line + "\n");
	}
	nstr.close();
	return content.length();
}

void AAsset_read(AAsset* asset, char* buffer, int size) {
	std::ifstream nstr(asset->filePath);
	nstr.read(buffer, size);
	nstr.close();
}

void AAsset_read(AAsset* asset, unsigned char* buffer, int size) {
	std::ifstream nstr(asset->filePath);
	std::string content;
	std::string line = "";

	while (!nstr.eof()) {
		std::getline(nstr, line);
		content.append(line + "\n");
	}

	memcpy(buffer, content.c_str(), size);
	nstr.close();
}

void AAsset_close(AAsset* asset) {
	delete asset;
}

const char* AAsset_asciiFilter(const char* source)
{
	int trimlen = 0;
	for (int i = 0; i < strlen(source); i++) {
		auto ch = static_cast<unsigned char>(source[i]);
		if (ch > 0x7F || ch < 0x04) { 
			trimlen = i; 
			break;
		}
	}

	char* buffer = new char[trimlen];
	memcpy(buffer, source, trimlen);
	return buffer;
}

AAssetManager::AAssetManager() {
	TCHAR s[256];
	GetCurrentDirectory(256, s);
	dir = s;
	dir += "\\asset";
}
#endif