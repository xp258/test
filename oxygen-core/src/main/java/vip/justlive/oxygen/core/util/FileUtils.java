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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import lombok.experimental.UtilityClass;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * 文件工具类
 *
 * @author wubo
 */
@UtilityClass
public class FileUtils {

  /**
   * 创建目录
   *
   * @param dir 目录
   */
  public static void mkdirs(File dir) {
    Checks.notNull(dir, "dir can not be null");
    if (!dir.mkdirs()) {
      throw Exceptions.fail("create dir fail");
    }
  }

  /**
   * 创建目录
   *
   * @param path 路径
   */
  public static void mkdirs(Path path) {
    Checks.notNull(path, "path can not be null");
    mkdirs(path.toFile());
  }

  /**
   * 创建目录
   *
   * @param path 路径
   */
  public static void mkdirs(String path) {
    Checks.notNull(path, "path can not be null");
    mkdirs(new File(path));
  }

  /**
   * 创建多级目录
   *
   * @param parent 父目录
   * @param children 子目录
   */
  public static void mkdirs(String parent, String... children) {
    Checks.notNull(parent, "parent can not be null");
    File parentDir = new File(parent);
    if (children != null && children.length > 0) {
      for (String child : children) {
        parentDir = new File(parentDir, child);
      }
    }
    mkdirs(parentDir);
  }

  /**
   * 判断文件父目录是否存在，不存在则创建
   *
   * @param file 文件
   */
  public static void mkdirsForFile(File file) {
    Checks.notNull(file, "file can not be null");
    File dir = file.getParentFile();
    if (dir != null && !dir.exists()) {
      mkdirs(dir);
    }
  }

  /**
   * 创建文件
   *
   * @param file 文件
   */
  public static void touch(File file) {
    Checks.notNull(file, "file can not be null");
    if (!file.exists()) {
      mkdirsForFile(file);
      try {
        if (!file.createNewFile()) {
          throw Exceptions.fail("touch file fail");
        }
      } catch (IOException e) {
        throw Exceptions.wrap(e);
      }
    }
  }

  /**
   * 创建文件
   *
   * @param path 文件路径
   */
  public static void touch(Path path) {
    Checks.notNull(path, "path can not be null");
    touch(path.toFile());
  }

  /**
   * 创建文件
   *
   * @param filepath 文件路径
   */
  public static void touch(String filepath) {
    Checks.notNull(filepath, "filepath can not be null");
    touch(new File(filepath));
  }

}
