
//
// Created by EmiyaSyahriel on 21/05/2021.
//
#pragma once
#include <cstdlib>
#include <vector>

template<typename T1, typename T2>
struct dictionary {
private:
    std::vector<std::pair<T1,T2>> _internaldata;
public:

    T2 const& operator[](T1 key);
    std::pair<T1, T2> *getKVP(T1 key);
    void add(T1 a, T2 b);
    void remove(T1 a);
    bool contains(T1 a);
};
