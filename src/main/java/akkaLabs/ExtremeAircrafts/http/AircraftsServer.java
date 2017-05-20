package akkaLabs.ExtremeAircrafts.http;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import akka.Done;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akkaLabs.ExtremeAircrafts.eventbus.PositionChangedHttpEntity;

public class AircraftsServer extends AllDirectives {

    private Map<UUID, PositionChangedHttpEntity> positionEvents = new ConcurrentHashMap<>();

    public Route createRoute() {
//        Map<UUID, PositionChangedEvent> tempMap = new HashMap<>(this.positionEvents);
//        Source<PositionChangedEvent, NotUsed> positionStream = Source.fromIterator(() -> tempMap.values().iterator());
        return route(
                get(()->
                        path("sky",()->
                                complete(StatusCodes.OK, positionEvents, Jackson.marshaller()))),
                post(()->
                        path("update",()->
                                entity(Jackson.unmarshaller(PositionChangedHttpEntity.class), event -> {
                                    CompletionStage<Done> futureUpdated = performUpdate(event);
                                    return onSuccess(() -> futureUpdated, done ->
                                        complete("aircraft position updated")
                            );}))));
    }

    private  CompletionStage<Done> performUpdate(PositionChangedHttpEntity event){
        positionEvents.put(event.getAircraftId(),event);
        return  CompletableFuture.completedFuture(Done.getInstance());
    }
}
