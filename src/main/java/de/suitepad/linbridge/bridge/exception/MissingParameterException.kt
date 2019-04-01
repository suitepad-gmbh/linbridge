package de.suitepad.linbridge.bridge.exception

import android.os.RemoteException

class MissingParameterException(parameterName: String): RemoteException("parameter $parameterName missing")