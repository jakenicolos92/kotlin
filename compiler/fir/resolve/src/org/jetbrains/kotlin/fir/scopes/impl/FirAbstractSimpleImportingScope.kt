/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.scopes.impl

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirResolvedImport
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.calls.tower.TowerScopeLevel
import org.jetbrains.kotlin.fir.resolve.firSymbolProvider
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassifierSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

abstract class FirAbstractSimpleImportingScope(
    session: FirSession,
    scopeSession: ScopeSession
) : FirAbstractImportingScope(session, scopeSession, lookupInFir = true) {

    // TODO try to hide this
    abstract val simpleImports: Map<Name, List<FirResolvedImport>>

    override fun processClassifiersByNameWithSubstitution(name: Name, processor: (FirClassifierSymbol<*>, ConeSubstitutor) -> Unit) {
        val imports = simpleImports[name] ?: return
        if (imports.isEmpty()) return
        val provider = session.firSymbolProvider
        for (import in imports) {
            val importedName = import.importedName ?: continue
            val classId =
                import.resolvedClassId?.createNestedClassId(importedName)
                    ?: ClassId.topLevel(import.packageFqName.child(importedName))
            val symbol = provider.getClassLikeSymbolByFqName(classId) ?: continue
            processor(symbol, ConeSubstitutor.Empty)
        }
    }

    override fun <T : FirCallableSymbol<*>> processCallables(
        name: Name,
        token: TowerScopeLevel.Token<T>,
        processor: (FirCallableSymbol<*>) -> Unit
    ) {
        val imports = simpleImports[name] ?: return
        if (imports.isEmpty()) return

        for (import in imports) {
            processCallables(import, import.importedName!!, token, processor)
        }
    }
}
