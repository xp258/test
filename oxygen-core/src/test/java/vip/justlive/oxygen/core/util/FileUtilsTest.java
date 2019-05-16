/*
 * Copyright (C) 2019 justlive1
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License
 *  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing permissions and limitations under
 *  the License.
 */
package vip.justlive.oxygen.core.util;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Paths;
import org.junit.Test;

public class FileUtilsTest {

  @Test(expected = NullPointerException.class)
  public void mkdirs() {
    File file = null;
    FileUtils.mkdirs(file);
  }

  @Test
  public void mkdirs1() {
    String name = "/tmp/a/1/1";
    File file = new File(name);
    FileUtils.mkdirs(file);
    assertTrue(file.exists());
    assertTrue(file.isDirectory());
    name = "/tmp/a/2/2";
    file = new File(name);
    FileUtils.mkdirs(name);
    assertTrue(file.exists());
    assertTrue(file.isDirectory());
    name = "/tmp/a/3/3";
    file = new File(name);
    FileUtils.mkdirs(Paths.get(name));
    assertTrue(file.exists());
    assertTrue(file.isDirectory());
  }

  @Test
  public void mkdirs2() {
    String parent = "/tmp/a/4";
    FileUtils.mkdirs(parent, "1", "2");
    File file = new File(parent + "/1/2");
    assertTrue(file.exists());
    assertTrue(file.isDirectory());
  }


  @Test
  public void mkdirsForFile() {
    FileUtils.mkdirsForFile(new File("/tmp/a/5/a.log"));
    File file = new File("/tmp/a/5");
    assertTrue(file.exists());
    assertTrue(file.isDirectory());
  }

  @Test
  public void touch() {
    FileUtils.touch("/tmp/a/6/a.log");
    File file = new File("/tmp/a/6/a.log");
    assertTrue(file.exists());
    assertTrue(file.isFile());
  }

}