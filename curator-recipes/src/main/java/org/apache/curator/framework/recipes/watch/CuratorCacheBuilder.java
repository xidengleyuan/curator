/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.curator.framework.recipes.watch;

import com.google.common.cache.CacheBuilder;
import org.apache.curator.framework.CuratorFramework;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class CuratorCacheBuilder
{
    private final CuratorFramework client;
    private final String path;
    private CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
    private boolean singleNode;
    private PrimingFilter primingFilter;
    private CacheFilter cacheFilter = new CacheFilter()
    {
        @Override
        public CacheAction actionForPath(String path)
        {
            return CacheAction.GET_DATA;
        }
    };

    public static CuratorCacheBuilder builder(CuratorFramework client, String path)
    {
        CuratorCacheBuilder builder = new CuratorCacheBuilder(client, path);
        return builder.forTree();
    }

    public CuratorCache build()
    {
        if ( singleNode )
        {
            return new InternalNodeCache(client, path, cacheFilter, cacheBuilder.<String, CachedNode>build());
        }
        return new InternalCuratorCache(client, path, cacheFilter, primingFilter, cacheBuilder.<String, CachedNode>build());
    }

    public CuratorCacheBuilder forSingleNode()
    {
        singleNode = true;
        primingFilter = null;
        return this;
    }

    public CuratorCacheBuilder forSingleLevel()
    {
        singleNode = false;
        primingFilter = new SingleLevelPrimingFilter(path);
        return this;
    }

    public CuratorCacheBuilder forTree()
    {
        singleNode = false;
        primingFilter = new TreePrimingFilter();
        return this;
    }

    public CuratorCacheBuilder usingWeakValues()
    {
        cacheBuilder = cacheBuilder.weakValues();
        return this;
    }

    public CuratorCacheBuilder usingSoftValues()
    {
        cacheBuilder = cacheBuilder.softValues();
        return this;
    }

    public CuratorCacheBuilder thatExpiresAfterWrite(long duration, TimeUnit unit)
    {
        cacheBuilder = cacheBuilder.expireAfterWrite(duration, unit);
        return this;
    }

    public CuratorCacheBuilder thatExpiresAfterAccess(long duration, TimeUnit unit)
    {
        cacheBuilder = cacheBuilder.expireAfterAccess(duration, unit);
        return this;
    }

    public CuratorCacheBuilder withCacheFilter(CacheFilter cacheFilter)
    {
        this.cacheFilter = Objects.requireNonNull(cacheFilter, "cacheFilter cannot be null");
        return this;
    }

    public CuratorCacheBuilder withPrimingFilter(PrimingFilter primingFilter)
    {
        this.primingFilter = Objects.requireNonNull(primingFilter, "primingFilter cannot be null");
        return this;
    }

    private CuratorCacheBuilder(CuratorFramework client, String path)
    {
        this.client = Objects.requireNonNull(client, "client cannot be null");
        this.path = Objects.requireNonNull(path, "path cannot be null");
    }
}