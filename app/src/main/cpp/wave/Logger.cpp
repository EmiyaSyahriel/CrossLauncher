
#include "Logger.h"
#include <cstdarg>
#include <stdio.h>

void W32_PRINT(char* format, ...) {
}

void W32_PRINT(char stype, char* tag, char* format, ...)
{
	va_list args;

	const int buffer_size = 256;
	char* buffer = new char[buffer_size];

	va_start(args, format);
	vsnprintf(buffer, buffer_size, format, args);
	va_end(args);

	printf("%c/%s %s\n", stype, tag, buffer);
}
