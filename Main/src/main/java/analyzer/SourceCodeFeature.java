package analyzer;

public enum SourceCodeFeature {
    primType,referType,arrayType,switchStmt,
    invocationStmt, ifStmt, tryStmt,  // control statment
//    throwStmt,
    loopDepthOne, // loop
    //    constructorCallStmt, assignmentStmt,
    //    arithmeticOperator, shiftOperator, compareOperator, logicalOperator, unaryOperator,
//    booleanType, byteType, charType, integerType, floatType, doubleType, longType, shortType, nullType, referenceType,
//    arrayDimensionOne, arrayDimensionTwo, arrayDimensionThreeOrMore,
//    fieldAccess, synchronizedAccess, enumValue, instance, lambda, returnStmt,
//    primitiveStmt,
//    binopStmt, unopStmt,
    arithmeticStmt,shiftStmt,logicalStmt, unopStmt,
    newStrongRef, newSoftRef, newWeakRef, newPhantomRef, newArray, newString, // add rule
    modifyStrongRef, modifySoftRef, modifyWeakRef, modifyPhantomRef, modifyArray, modifyString, // modify rule
    invokeModifyStrongRef, invokeModifySoftRef, invokeModifyWeakRef, invokeModifyPhantomRef, invokeModifyArray, invokeModifyString, // modify rule

    assignNullStrongRef, assignNullSoftRef, assignNullWeakRef, assignNullPhantomRef, assignNullArray, assignNullString // delete rule

}