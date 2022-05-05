// package com.example;
//
// import com.oracle.svm.core.annotate.AutomaticFeature;
// import com.oracle.svm.core.jdk.RuntimeSupport;
// import com.oracle.svm.core.util.UserError;
// import com.oracle.svm.core.util.VMError;
// import com.oracle.svm.util.ReflectionUtil;
// import com.sun.management.HotSpotDiagnosticMXBean;
// import org.graalvm.nativeimage.Platform;
// import org.graalvm.nativeimage.hosted.Feature;
// import com.sun.management.internal.PlatformMBeanProviderImpl;
// import com.oracle.svm.core.jfr.JfrManager;
// import org.graalvm.nativeimage.hosted.Feature.IsInConfigurationAccess;
//
// @AutomaticFeature
// public class MyFeature implements Feature{
//
//     static {
//         System.out.println("MyFEATURE");
//     }
//
//     public void beforeAnalysis(Feature.BeforeAnalysisAccess access) {
//         RuntimeSupport runtime = RuntimeSupport.getRuntimeSupport();
//         JfrManager manager = JfrManager.get();
//         runtime.addStartupHook(manager::setup);
//         runtime.addShutdownHook(manager::teardown);
//     }
//
//     private static final boolean HOSTED_ENABLED = Boolean.parseBoolean(getDiagnosticBean().getVMOption("FlightRecorder").getValue());
//
//     // @Override
//     public boolean isInConfiguration(IsInConfigurationAccess access) {
//         boolean systemSupported = osSupported();
//         if (HOSTED_ENABLED && !systemSupported) {
//             throw UserError.abort("FlightRecorder cannot be used to profile the image generator on this platform. " +
//                     "The image generator can only be profiled on platforms where FlightRecoder is also supported at run time.");
//         }
//         return true;
//     }
//
//     private static boolean osSupported() {
//         return Platform.includedIn(Platform.LINUX.class) || Platform.includedIn(Platform.DARWIN.class);
//     }
//
//     private static HotSpotDiagnosticMXBean getDiagnosticBean() {
//         System.out.println("HELLLOOOOOO");
//         try {
//             return (HotSpotDiagnosticMXBean) ReflectionUtil.lookupMethod(PlatformMBeanProviderImpl.class, "getDiagnosticMXBean").invoke(null);
//         } catch (ReflectiveOperationException ex) {
//             throw VMError.shouldNotReachHere(ex);
//         }
//     }
// }

/*
 * Copyright (c) 2019, 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.example;

import com.oracle.svm.core.VMInspectionOptions;
import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.hub.DynamicHub;
import com.oracle.svm.core.hub.DynamicHubSupport;
import com.oracle.svm.core.jdk.RuntimeSupport;
// import com.oracle.svm.core.jfr.JfrFrameTypeSerializer;
// import com.oracle.svm.core.jfr.JfrGCNames;
// import com.oracle.svm.core.jfr.JfrManager;
// import com.oracle.svm.core.jfr.JfrSerializerSupport;
// import com.oracle.svm.core.jfr.JfrThreadStateSerializer;
// import com.oracle.svm.core.jfr.SubstrateJVM;
// import com.oracle.svm.core.jfr.traceid.JfrTraceId;
// import com.oracle.svm.core.jfr.traceid.JfrTraceIdEpoch;
// import com.oracle.svm.core.jfr.traceid.JfrTraceIdMap;
import com.oracle.svm.core.meta.SharedType;
import com.oracle.svm.core.thread.ThreadListenerFeature;
import com.oracle.svm.core.thread.ThreadListenerSupport;
import com.oracle.svm.core.util.UserError;
import com.oracle.svm.core.util.VMError;
import com.oracle.svm.hosted.FeatureImpl;
import com.oracle.svm.hosted.FeatureImpl.DuringAnalysisAccessImpl;
import com.oracle.svm.util.ModuleSupport;
import com.oracle.svm.util.ReflectionUtil;
import com.sun.management.HotSpotDiagnosticMXBean;
import com.sun.management.internal.PlatformMBeanProviderImpl;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import jdk.jfr.Configuration;
import jdk.jfr.Event;
import jdk.jfr.internal.JVM;
import jdk.jfr.internal.jfc.JFC;
// import jdk.vm.ci.meta.MetaAccessProvider;
// import jdk.vm.ci.meta.MetaAccessProvider;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.Platform;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.graalvm.nativeimage.hosted.RuntimeReflection;
import org.graalvm.nativeimage.impl.RuntimeClassInitializationSupport;

@AutomaticFeature
public class MyFeature implements Feature {
    /*
     * Note that we could initialize the native part of JFR at image build time and that the native
     * code sets the FlightRecorder option as a side effect. Therefore, we must ensure that we check
     * the value of the option before it can be affected by image building.
     */
    private static final boolean HOSTED_ENABLED = Boolean.parseBoolean(getDiagnosticBean().getVMOption("FlightRecorder").getValue());

    @Override
    public boolean isInConfiguration(IsInConfigurationAccess access) {
        boolean systemSupported = osSupported();
        if (HOSTED_ENABLED && !systemSupported) {
            throw UserError.abort("FlightRecorder cannot be used to profile the image generator on this platform. " +
                    "The image generator can only be profiled on platforms where FlightRecoder is also supported at run time.");
        }
        boolean runtimeEnabled = VMInspectionOptions.AllowVMInspection.getValue();
        if (HOSTED_ENABLED && !runtimeEnabled) {
            System.err.println("Warning: When FlightRecoder is used to profile the image generator, it is also automatically enabled in the native image at run time. " +
                    "This can affect the measurements because it can can make the image larger and image build time longer.");
            runtimeEnabled = true;
        }
        return runtimeEnabled && systemSupported;
    }

    private static boolean osSupported() {
        return Platform.includedIn(Platform.LINUX.class) || Platform.includedIn(Platform.DARWIN.class);
    }

    /**
     * We cannot use the proper way of looking up the bean via
     * {@link java.lang.management.ManagementFactory} because that initializes too many classes at
     * image build time that we want to initialize only at run time.
     */
    private static HotSpotDiagnosticMXBean getDiagnosticBean() {
        try {
            return (HotSpotDiagnosticMXBean) ReflectionUtil.lookupMethod(PlatformMBeanProviderImpl.class, "getDiagnosticMXBean").invoke(null);
        } catch (ReflectiveOperationException ex) {
            throw VMError.shouldNotReachHere(ex);
        }
    }

    @Override
    public List<Class<? extends Feature>> getRequiredFeatures() {
        return Collections.singletonList(ThreadListenerFeature.class);
    }

    @Override
    public void afterRegistration(AfterRegistrationAccess access) {
        ModuleSupport.exportAndOpenAllPackagesToUnnamed("jdk.jfr", false);
        ModuleSupport.exportAndOpenAllPackagesToUnnamed("java.base", false);
        ModuleSupport.exportAndOpenPackageToClass("jdk.jfr", "jdk.jfr.events", false, MyFeature.class);
        // ModuleSupport.exportAndOpenPackageToClass("jdk.internal.vm.ci", "jdk.vm.ci.hotspot", false, JfrEventSubstitution.class);

        // Initialize some parts of JFR/JFC at image build time.
        List<Configuration> knownConfigurations = JFC.getConfigurations();
        JVM.getJVM().createNativeJFR();

        // ImageSingletons.add(JfrManager.class, new JfrManager(HOSTED_ENABLED));
        // ImageSingletons.add(SubstrateJVM.class, new SubstrateJVM(knownConfigurations));
        // ImageSingletons.add(JfrSerializerSupport.class, new JfrSerializerSupport());
        // ImageSingletons.add(JfrTraceIdMap.class, new JfrTraceIdMap());
        // ImageSingletons.add(JfrTraceIdEpoch.class, new JfrTraceIdEpoch());
        // ImageSingletons.add(JfrGCNames.class, new JfrGCNames());
        //
        // JfrSerializerSupport.get().register(new JfrFrameTypeSerializer());
        // JfrSerializerSupport.get().register(new JfrThreadStateSerializer());
        // ThreadListenerSupport.get().register(SubstrateJVM.getThreadLocal());

        if (HOSTED_ENABLED) {
            RuntimeClassInitializationSupport rci = ImageSingletons.lookup(RuntimeClassInitializationSupport.class);
            rci.initializeAtBuildTime("jdk.management.jfr", "Allow FlightRecorder to be used at image build time");
            rci.initializeAtBuildTime("com.sun.jmx.mbeanserver", "Allow FlightRecorder to be used at image build time");
            rci.initializeAtBuildTime("com.sun.jmx.defaults", "Allow FlightRecorder to be used at image build time");
            rci.initializeAtBuildTime("java.beans", "Allow FlightRecorder to be used at image build time");
        }
    }

    @Override
    public void duringSetup(DuringSetupAccess c) {
        FeatureImpl.DuringSetupAccessImpl config = (FeatureImpl.DuringSetupAccessImpl) c;
        // MetaAccessProvider metaAccess = config.getMetaAccess().getWrapped();

        for (Class<?> eventSubClass : config.findSubclasses(Event.class)) {
            RuntimeClassInitialization.initializeAtBuildTime(eventSubClass.getName());
        }
        // config.registerSubstitutionProcessor(new JfrEventSubstitution(metaAccess));
    }

    @Override
    public void beforeAnalysis(Feature.BeforeAnalysisAccess access) {
        RuntimeSupport runtime = RuntimeSupport.getRuntimeSupport();
        // JfrManager manager = JfrManager.get();
        // runtime.addStartupHook(manager.startupHook());
        // runtime.addShutdownHook(manager.shutdownHook());

        Class<?> eventClass = access.findClassByName("jdk.internal.event.Event");
        if (eventClass != null) {
            access.registerSubtypeReachabilityHandler(MyFeature::eventSubtypeReachable, eventClass);
        }

    }

    @Override
    public void beforeCompilation(BeforeCompilationAccess a) {
        // Reserve slot 0 for error-catcher.
        int mapSize = ImageSingletons.lookup(DynamicHubSupport.class).getMaxTypeId() + 1;

        // Create trace-ID map with fixed size.
        // ImageSingletons.lookup(JfrTraceIdMap.class).initialize(mapSize);

        // Scan all classes and build sets of packages, modules and class-loaders. Count all items.
        Collection<? extends SharedType> types = ((FeatureImpl.CompilationAccessImpl) a).getTypes();
        for (SharedType type : types) {
            // DynamicHub hub = type.getHub();
            // Class<?> clazz = hub.getHostedJavaClass();
            // Off-set by one for error-catcher
            // JfrTraceId.assign(clazz, hub.getTypeID() + 1);
        }
    }

    private static void eventSubtypeReachable(DuringAnalysisAccess a, Class<?> c) {
        DuringAnalysisAccessImpl access = (DuringAnalysisAccessImpl) a;
        if (c.getCanonicalName().equals("jdk.jfr.Event") ||
                c.getCanonicalName().equals("jdk.internal.event.Event") ||
                c.getCanonicalName().equals("jdk.jfr.events.AbstractJDKEvent") ||
                c.getCanonicalName().equals("jdk.jfr.events.AbstractBufferStatisticsEvent")) {
            return;
        }
        try {
            Field f = c.getDeclaredField("eventHandler");
            RuntimeReflection.register(f);
            // access.rescanRoot(f);
            a.requireAnalysisIteration();
        } catch (Exception e) {
            throw VMError.shouldNotReachHere("Unable to register eventHandler for: " + c.getCanonicalName(), e);
        }
    }
}