//
// Created by Marceau Tonelli on 13/11/2025.
// Copyright (c) 2025 orgName. All rights reserved.
//

import Foundation

@objc(DeviceUtil)
public class DeviceUtil: NSObject {

    @objc static public func isRunningOnSimulator() -> Bool {
        #if targetEnvironment(simulator)
        return true
        #else
        return false
        #endif
    }
}
