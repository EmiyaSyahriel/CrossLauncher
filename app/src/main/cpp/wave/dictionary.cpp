
//
// Created by EmiyaSyahriel on 21/05/2021.
//

#include "dictionary.h"


template<typename T1, typename T2>
T2 const &dictionary<T1, T2>::operator[](T1 key) {
    return *getKVP(key).second;
}

template<typename T1, typename T2>
void dictionary<T1, T2>::add(T1 a, T2 b) {
    if(getKVP(a) == nullptr){
        _internaldata.push_back(std::pair<T1, T2>(a,b));
    }
}

template<typename T1, typename T2>
std::pair<T1, T2>* dictionary<T1, T2>::getKVP(T1 key) {
    for(std::pair<T1, T2> data : _internaldata){
        if(key == data.first) return &data;
    }
    return nullptr;
}

template<typename T1, typename T2>
void dictionary<T1, T2>::remove(T1 a) {
    int index = -1;
    for(int i =0 ; i < _internaldata; i++){ if(_internaldata[i].first == a) index = i; }
    if(index != -1){
        delete *_internaldata[index];
        _internaldata.erase(_internaldata.begin() + index);
    }
}

template<typename T1, typename T2>
bool dictionary<T1, T2>::contains(T1 a) {
    return getKVP(a) != nullptr;
}
