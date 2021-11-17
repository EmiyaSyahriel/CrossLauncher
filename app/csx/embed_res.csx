#!/usr/bin/env dotnet-script

/**
    This build script is licensed under CC0 1.0 (Non-copyrighted Public Domain).
    Therefore, you can copy, modify and use this build script to fulfill your need
    without giving any credit to me (as "EmiyaSyahriel"). I have no responsibility
    on any damage caused by this script in any way.
 
    For additional info, Please refer to "https://creativecommons.org/publicdomain/zero/1.0/"
 */

/** 
This script requires .NET Scripting Tools, please refer to "https://github.com/filipw/dotnet-script#installing" for installation
  
Usages : 
from .NET Script:
    dotnet script "path/to/this_script.csx" -- [cpp_namespace] [cpp_delimiter] [output_dir] [output_filename] [res_files] [source_type_conv]
from Shell:
    "path/to/this_script.csx" [cpp_namespace] [cpp_delimiter] [output_dir] [output_filename] [res_files] [source_type_conv]

cpp_namespace : C++ Namespace, giving an empty string ("") will cause the generated 
                source to not use any namespace

cpp_delimiter : C++ Multiline string delimiter
output_dir : Where the source files will be generated
output_filename : What name will the source file being named as, without file extension
res_files : Resource file paths, semicolon-separated.
source_type_conv : File Extension Convention, Semicolor-separated with the first is the Source file and the Second is header file. by default "cpp;hpp"
 */

using System;
using System.Text;
using System.IO;
using System.Collections.Generic;

Console.WriteLine("C++ Text Resource Embedder");
Console.WriteLine($"\tWork dir : {Environment.CurrentDirectory}");

string hpp_type = "hpp", cpp_type = "cpp";

if (Args.Count >= 6)
{
    string[] types = Args[5].Split(';');
    if (types.Length >= 1) cpp_type = types[0];
    if (types.Length >= 2) hpp_type = types[1];
}
Console.WriteLine($"\tNaming Convention : .{cpp_type} as Source, .{hpp_type} as Header");

string hpp_path = Path.Combine(Environment.CurrentDirectory, Args[2], $"{Args[3]}.{hpp_type}"),
       cpp_path = Path.Combine(Environment.CurrentDirectory, Args[2], $"{Args[3]}.{cpp_type}");

Console.WriteLine($"\tWork dir : {Environment.CurrentDirectory}");
Console.WriteLine($"\tTarget source path : {cpp_path} / {hpp_path}");

string[] source_files = Args[4].Split(';');
Console.WriteLine($"\tEmbedding {source_files.Length} files into one file");
const string AUTO_GEN_TEMPLATE = "//// AUTO-GENERATED RESOURCE FILE ////\n//// File auto-generated using C# Script at \"app/csx/embed_res.csx\" ////";

StringBuilder cpp_content = new StringBuilder(), hpp_content = new StringBuilder();

string include_guard = $"RES_{Args[3].ToUpper()}_HPP";

Console.WriteLine($"\tNamespace : {Args[0]}");

hpp_content.AppendLine($"#pragma once");
hpp_content.AppendLine($"#ifndef {include_guard}");
hpp_content.AppendLine($"#define {include_guard}");

cpp_content.AppendLine($"#include \"{Args[3]}.{hpp_type}\"");
cpp_content.AppendLine(AUTO_GEN_TEMPLATE);
hpp_content.AppendLine(AUTO_GEN_TEMPLATE);
hpp_content.Append("\n\n");

bool use_ns = !string.IsNullOrEmpty(Args[0]);
bool use_line_limit = !string.IsNullOrEmpty(Args[1]);

string line_limit_pre = "", line_limit_suf = "";

if (use_ns)
{
    cpp_content.AppendLine($"namespace {Args[0]} {{");
    hpp_content.AppendLine($"namespace {Args[0]} {{");
}

if (use_line_limit)
{
    line_limit_pre = $"{Args[1]}";
    line_limit_suf = $"{Args[1]}";
}

string MakeVarName(string path) => Path.GetFileName(path).Replace(".", "_");

foreach (string src_path in source_files)
{
    string varname = MakeVarName(src_path);
    string content = File.ReadAllText(src_path);
    Console.WriteLine($"\t\tGenerating \"{src_path}\" as {Args[0]}::{varname}");
    if (use_ns)
    {
        hpp_content.Append('\t');
        cpp_content.Append('\t');
    }

    hpp_content.AppendLine($"extern const char* const {varname};");
    cpp_content.AppendLine($"const char* const {varname} = R\"{line_limit_pre}({content}){line_limit_suf}\";");
}

if (use_ns)
{
    cpp_content.AppendLine($"}}");
    hpp_content.AppendLine($"}}");
}

hpp_content.AppendLine($"#endif // {include_guard}");

File.WriteAllText(hpp_path, hpp_content.ToString());
File.WriteAllText(cpp_path, cpp_content.ToString());