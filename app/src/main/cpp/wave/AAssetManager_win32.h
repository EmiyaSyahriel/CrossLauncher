#if WIN32
#pragma once

#include <cstdio>
#include <filesystem>
#include <iostream>
#include <fstream>
#include <string>
#include <windows.h>

enum ASSET_STREAM {
	AASSET_MODE_UNKNOWN = 0,
	AASSET_MODE_STREAMING = 1
};

class AAssetManager { 
public:
	std::string dir;
	AAssetManager();
};

class AAsset {
public:
	std::string filePath;
	AAsset(std::string source);
};

AAsset* AAssetManager_open(AAssetManager* mgr, std::string filename, ASSET_STREAM mode);

int AAsset_getLength(AAsset* asset);

void AAsset_read(AAsset* asset, char* buffer, int size);

void AAsset_read(AAsset* asset, unsigned char* buffer, int size);

void AAsset_close(AAsset* asset);

const char* AAsset_asciiFilter(const char* source);

#endif