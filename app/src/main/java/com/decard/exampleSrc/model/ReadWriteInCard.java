package com.decard.exampleSrc.model;

import com.decard.exampleSrc.FelicaCard;

public interface ReadWriteInCard {
    public FelicaCard readData() throws Exception;
    public int writeInCard(int serviceNum, byte[] serviceList, int blockNum, byte[] blockList,byte[] blockData) throws Exception;
}