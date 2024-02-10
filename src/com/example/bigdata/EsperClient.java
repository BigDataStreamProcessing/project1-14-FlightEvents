package com.example.bigdata;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompiler;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.*;
import net.datafaker.Faker;
import net.datafaker.fileformats.Format;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class EsperClient {
    public static void main(String[] args) throws InterruptedException {
        int noOfRecordsPerSec;
        int howLongInSec;

        // Parse CMD arguments
        if (args.length < 2) {
            noOfRecordsPerSec = 2;
            howLongInSec = 5;
        } else {
            noOfRecordsPerSec = Integer.parseInt(args[0]);
            howLongInSec = Integer.parseInt(args[1]);
        }

        Configuration config = new Configuration();
        EPCompiled epCompiled = getEPCompiled(config);

        // Connect to the EPRuntime server and deploy the statement
        EPRuntime runtime = EPRuntimeProvider.getRuntime("http://localhost:port", config);
        EPDeployment deployment;
        try {
            deployment = runtime.getDeploymentService().deploy(epCompiled);
        }
        catch (EPDeployException ex) {
            // handle exception here
            throw new RuntimeException(ex);
        }

        EPStatement resultStatement = runtime.getDeploymentService().getStatement(deployment.getDeploymentId(), "answer");

        // Add a listener to the statement to handle incoming events
        resultStatement.addListener( (newData, oldData, stmt, runTime) -> {
            for (EventBean eventBean : newData) {
                System.out.printf("IN: %s%n", eventBean.getUnderlying());
            }
            for (EventBean eventBean : oldData  ) {
                System.out.printf("OUT: %s%n", eventBean.getUnderlying());
            }
        });

        // Random events generated from Faker.
        Faker faker = new Faker();
        String record;

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < startTime + (1000L * howLongInSec)) {
            for (int i = 0; i < noOfRecordsPerSec; i++) {
                Timestamp eTimestamp = faker.date().past(30, TimeUnit.SECONDS);
                Timestamp iTimestamp = Timestamp.valueOf(LocalDateTime.now().withNano(0));
                eTimestamp.setNanos(0);
                record = Format.toJson()
                        .set("aircraft", () -> faker.aviation().aircraft())
                        .set("airline", () -> faker.aviation().airport())
                        .set("airport", () -> faker.aviation().airport())
                        .set("flight", () -> faker.aviation().flight())
                        .set("clouds", EsperClient::cloudCoverage)
                        .set("wind_dir", EsperClient::windDirection)
                        .set("wind_str", EsperClient::windStrength)
                        .set("ets", eTimestamp::toString)
                        .set("its", iTimestamp::toString)
                        .build().generate();
                runtime.getEventService().sendEventJson(record, "FlightEvent");
            }
            waitToEpoch();
        }
    }

    private static EPCompiled getEPCompiled(Configuration config) {
        CompilerArguments compilerArgs = new CompilerArguments(config);

        // Compile the EPL statement
        EPCompiler compiler = EPCompilerProvider.getCompiler();
        EPCompiled epCompiled;
        try {
            epCompiled = compiler.compile("""
                    @public @buseventtype create json schema
                    FlightEvent(aircraft string, airline string, airport string, flight string, clouds string, wind_dir string, wind_str double, ets string, its string);
                    @name('answer') SELECT aircraft, airline, airport, flight, clouds, wind_dir, wind_str, ets, its
                    FROM FlightEvent#ext_timed(java.sql.Timestamp.valueOf(its).getTime(), 3 sec);""",
                    compilerArgs);
    }
        catch (EPCompileException ex) {
            // handle exception here
            throw new RuntimeException(ex);
        }
        return epCompiled;
    }

    static void waitToEpoch() throws InterruptedException {
        long millis = System.currentTimeMillis();
        Instant instant = Instant.ofEpochMilli(millis) ;
        Instant instantTrunc = instant.truncatedTo( ChronoUnit.SECONDS ) ;
        long millis2 = instantTrunc.toEpochMilli() ;
        TimeUnit.MILLISECONDS.sleep(millis2+1000-millis);
    }

    static String cloudCoverage(){
        double number = Math.random();

        if (number < 0.005)
            return "OVC";
        else if (number < 0.1)
            return "BKN";
        else if (number < 0.2)
            return "SCT";
        else if (number < 0.7)
            return "FEW";
        else if (number < 0.85)
            return "NSC";
        else
            return "SKC";
    }

    static String windDirection(){
        double v = Math.random();

        if (v < 0.125)
            return "N";
        if (v < 0.25)
            return  "NE";
        if (v < 0.375)
            return "E";
        if (v < 0.5)
            return "SE";
        if (v < 0.625)
            return "S";
        if (v < 0.75)
            return "SW";
        if (v < 0.875)
            return "W";
        else
            return "NW";
    }

    static double windStrength(){
        Random norm = new Random();

        double v = norm.nextGaussian();
        v = Math.abs((v * 75) + 50);

        BigDecimal bd = BigDecimal.valueOf(v);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}

