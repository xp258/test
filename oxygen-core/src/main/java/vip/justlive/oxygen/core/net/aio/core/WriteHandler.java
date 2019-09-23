/*
 * Copyright (C) 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package vip.justlive.oxygen.core.net.aio.core;

import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;

/**
 * aio写操作处理
 *
 * @author wubo
 */
@RequiredArgsConstructor
public class WriteHandler implements CompletionHandler<Integer, CompletableFuture<Void>> {

  private final ChannelContext channelContext;

  @Override
  public void completed(Integer result, CompletableFuture<Void> future) {
    future.complete(null);
    channelContext.setLastSentAt(System.currentTimeMillis());
  }

  @Override
  public void failed(Throwable exc, CompletableFuture<Void> future) {
    future.completeExceptionally(exc);
  }
}
