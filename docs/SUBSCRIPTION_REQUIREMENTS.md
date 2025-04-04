# Requirements document GraphQL subscriptions feature

The high-level goal is to be able to provide a GraphQL subscriptions
API for vehicles and stations. This will enable clients to subscribe
to data updates and avoid polling strategies.

## High-level description of solution

A client will initiate a subscription via GraphQL with a set of arguments
limiting the subscription to a geographical area, among other things.

The server listens to changes in the cache, distributes those changes
to subscription handlers which will filter updates according to the 
subscription arguments.

Finally, the subscription handler will pass those updates on to the client.

## Data initialization

We need to decide between two data initialization strategies:

1. When the subscription is created, the client will receive all the 
   current data matching the subscription arugments.
2. The client will need to query initially using a regular GraphQL query
   which will need to contain a cursor that can be used in the 
   subscription to limit the initial data.

The are several problems with the second approach, but the most important
problem is deciding on a cursor-implementation without the solution
getting too complicated.

Keeping this in mind, I think the first approach makes the most sense,
but due to limitations in our current api gateway, we might need to
consider a cursor-based option (maybe optional) as a plan B.

## API

The subscription API should look someone like this:

```graphql

type Subscription {
    vehicles(
        lat: Float
        lon: Float
        range: Int
        minimumLatitude: Float
        minimumLongitude: Float
        maximumLatitude: Float
        maximumLongitude: Float
        codespaces: [String]
        systems: [String]
        operators: [String]
        formFactors: [FormFactor]
        propulsionTypes: [PropulsionType]
        includeReserved: Boolean = false
        includeDisabled: Boolean = false
    ): [VehicleUpdate]
    
    stations(
        lat: Float
        lon: Float
        range: Int
        minimumLatitude: Float
        minimumLongitude: Float
        maximumLatitude: Float
        maximumLongitude: Float
        codespaces: [String]
        systems: [String]
        operators: [String]
        availableFormFactors: [FormFactor]
        availablePropulsionTypes: [PropulsionType]
    ): [StationUpdate]
} 


type VehicleUpdate {
    vehicleId: String!
    updateType: UpdateType!
    vehicle: Vehicle
}

type StationUpdate {
    stationId: String!
    updateType: UpdateType!
    station: Station
}

enum UpdateType {
    CREATE,
    UPDATE,
    DELETE
}
```

The filtering arguments are the same as in the corresponding queries, except
there is no option to select `ids` or `count`.

The semantics are simple: we need to know the id of the entity, the update type
and the entity itself (provided the update type is not DELETE).

## Considerations on the cache listener

Redisson offers way to listen to changes to caches. Specifically the RMapCache
that backs the EntityCache implementation has the capability to add listeners.
We should look into leveraging this.

The EntityCache already has a read interface (EntityReader). I think it
would be good to also add an EntityListener interface, which would expose
methods for listening to changes in the EntityCache.

