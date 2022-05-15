package com.example.panbackend;

import com.example.panbackend.service.FileService;
import com.example.panbackend.service.impl.FileServiceImpl;

public class deleteTest {


    public static void main(String[] args) {
        FileService service = new FileServiceImpl();
        service.fileDelete("E:\\Java\\IO\\aaa",1);

    }
}
