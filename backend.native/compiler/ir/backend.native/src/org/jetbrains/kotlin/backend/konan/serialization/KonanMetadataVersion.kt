package org.jetbrains.kotlin.backend.konan.serialization

import org.jetbrains.kotlin.metadata.deserialization.BinaryVersion


/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class KonanMetadataVersion(vararg numbers: Int): BinaryVersion(*numbers){
    override fun isCompatible(): Boolean = this.major == 1 && this.minor == 0
    companion object {
        @JvmField
        val INSTANCE = KonanMetadataVersion(1, 0, 0)

        @JvmField
        val INVALID_VERSION = KonanMetadataVersion()
    }

}