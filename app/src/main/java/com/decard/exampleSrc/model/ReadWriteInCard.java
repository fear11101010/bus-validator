package com.decard.exampleSrc.model;

import com.decard.exampleSrc.FelicaCard;

public interface ReadWriteInCard {
    FelicaCard readData() throws Exception;
    int writeInCard(int serviceNum, byte[] serviceList, int blockNum, byte[] blockList, byte[] blockData) throws Exception;
}