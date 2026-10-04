package com.tht.split.core;
import org.junit.Test;
import static org.junit.Assert.*;
public class TaskParserTest {
 @Test public void android9TaskRecord(){assertEquals(51,TaskParser.find("TaskRecord{ab #51 A=com.example.map U=0 StackId=3}","com.example.map"));}
 @Test public void modernTaskUidPrefix(){assertEquals(93,TaskParser.find("Task{abc #93 type=standard A=10123:com.example.player U=0}","com.example.player"));}
 @Test public void exactPackageNeverMatchesAnotherApp(){assertEquals(-1,TaskParser.find("TaskRecord{abc #51 A=com.example.maps U=0}","com.example.map"));}
 @Test public void missingTaskIsExplicitFailure(){assertEquals(-1,TaskParser.find("no recent tasks","com.example.map"));}
 @Test public void supportsComponentName(){assertEquals(8,TaskParser.find("TaskRecord{abc #8 A=com.example.map/.Main U=0}","com.example.map"));}
}
