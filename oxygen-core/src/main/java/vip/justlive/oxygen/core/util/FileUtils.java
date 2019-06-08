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
import java.util.function.Predicate;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * 文件工具类
 *
 * @author wubo
 */
@Slf4j
@UtilityClass
public class FileUtils {

  /**
   * 创建目录
   *
   * @param dir 目录
   */
  public static void mkdirs(File dir) {
    Checks.notNull(dir);
    if (dir.exists()) {
      log.info("mkdirs [{}] exists", dir);
    } else {
      if (!dir.mkdirs()) {
        throw Exceptions.fail("create dir fail");
      }
      log.info("mkdirs [{}] successfully", dir);
    }
  }

  /**
   * 创建目录
   *
   * @param path 路径
   */
  public static void mkdirs(Path path) {
    Checks.notNull(path);
    mkdirs(path.toFile());
  }

  /**
   * 创建目录
   *
   * @param path 路径
   */
  public static void mkdirs(String path) {
    Checks.notNull(path);
    mkdirs(new File(path));
  }

  /**
   * 创建多级目录
   *
   * @param parent 父目录
   * @param children 子目录
   */
  public static void mkdirs(String parent, String... children) {
    Checks.notNull(parent);
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
    Checks.notNull(file);
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
    Checks.notNull(file);
    if (!file.exists()) {
      mkdirsForFile(file);
      try {
        if (!file.createNewFile()) {
          throw Exceptions.fail("touch file fail");
        }
        log.info("file [{}] created", file);
      } catch (IOException e) {
        throw Exceptions.wrap(e);
      }
    } else {
      log.info("file [{}] exists", file);
    }
  }

  /**
   * 创建文件
   *
   * @param path 文件路径
   */
  public static void touch(Path path) {
    Checks.notNull(path);
    touch(path.toFile());
  }

  /**
   * 创建文件
   *
   * @param filepath 文件路径
   */
  public static void touch(String filepath) {
    touch(new File(Checks.notNull(filepath)));
  }

  /**
   * 获取文件扩展名
   *
   * @param filename 文件名
   * @return extension
   */
  public static String extension(String filename) {
    int index = Checks.notNull(filename).lastIndexOf(Constants.DOT);
    if (index == -1) {
      return Constants.EMPTY;
    }
    return filename.substring(index + 1);
  }

  /**
   * count files
   *
   * @param file 文件
   * @return count
   */
  public static int countFiles(File file) {
    return countFiles(file, MoreObjects.alwaysTrue());
  }

  /**
   * count files
   *
   * @param file 文件
   * @param filter 过滤器
   * @return count
   */
  public static int countFiles(File file, Predicate<File> filter) {
    Checks.notNull(file, "file cant not be null");
    Checks.notNull(filter, "filter can not be null");
    if (file.isFile()) {
      if (filter.test(file)) {
        return 1;
      }
      return 0;
    } else {
      File[] files = file.listFiles();
      if (files == null || files.length == 0) {
        return 0;
      }
      int count = 0;
      for (File child : files) {
        count += countFiles(child, filter);
      }
      return count;
    }
  }

  /**
   * count dirs
   *
   * @param file 文件
   * @return count
   */
  public static int countDirs(File file, Predicate<File> filter) {
    Checks.notNull(file, "file cant not be null");
    Checks.notNull(filter, "filter can not be null");
    if (file.isDirectory()) {
      File[] files = file.listFiles();
      if (files == null || files.length == 0) {
        if (filter.test(file)) {
          return 1;
        }
        return 0;
      }
      int count = 1;
      for (File child : files) {
        count += countDirs(child, filter);
      }
      return count;
    } else {
      return 0;
    }
  }


}
