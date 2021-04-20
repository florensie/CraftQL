# CraftQL
CraftQL is a server-side Minecraft mod that allows querying and mutating game objects through a schema-driven GraphQL API.
The project is currently quite experimental, proceed with caution.

## Installation
This mod requires Fabric API and Fabric Language Kotlin to be installed.
Several other libraries for GraphQL and a HTTP/Websocket server are included.

## Usage
The GraphQL API is exposed at `http://localhost:8080/graphql`. You can start using the mod immediately with the
[base schema] or you can choose to [replace/expand this schema through a datapack](#custom-schemas).

### Demo
Here you can find a 3-minute video that demonstrates the combined power of a GraphQL API and Minecraft.

[![Demonstration video thumbnail](https://img.youtube.com/vi/N04EBNkHH2g/0.jpg)](https://www.youtube.com/watch?v=N04EBNkHH2g)

## Custom Schemas
There is just one small [base schema] included in the mod. Chances are this doesn't fit your needs,
which is why schemas are supplied fully data-driven.

> Note: the location of this base schema might change in the future

You can make a datapack to add or replace schemas by placing your `.graphqls` file(s) at this path: `data/[namespace]/schemas/`.
This also means you can use `/reload` to reload your schema while your server is still running.

In short, your schema is resolved to Java objects, fields and methods like this:
- Types are resolved to Java objects purely through the graph structure of the schema, naming does not matter here
- Fields are automatically resolved to fields/methods on their backing Java object by remapping their names at runtime through Yarn

### Example
Consider the following GraphQL schema:

```graphqls
extend type ServerPlayerEntity {
  experienceLevel: Int!
  statusEffects: [EffectInstance!]!
}

type EffectInstance {
  duration: Int!
}
```

- `experienceLevel` will be resolved to the field with the same name on `PlayerEntity`
- `statusEffects` will be resolved to the method `getStatusEffects` on `LivingEntity`
  - `getStatusEffects` returns `Collection<StatusEffectInstance>`, which corresponds to the field's return type in our schema: `[EffectInstance!]!`

> Note: extending/replacing another schema's types might be a bit janky right now

[base schema]: src/main/resources/data/minecraft/schemas/base.graphqls
