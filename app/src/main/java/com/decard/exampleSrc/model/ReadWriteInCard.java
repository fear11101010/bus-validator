package com.decard.exampleSrc.model;

public interface ReadWriteInCard {
    public int readData() throws Exception;
    public int writeInCard(int serviceNum, byte[] serviceList, int blockNum, byte[] blockList,byte[] blockData) throws Exception;
}