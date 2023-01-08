# Tiny ORM

> **Disclaimer** I'm not sure this project is a great idea. But it's a fun.

## What is the problem?

ORM tools like Hibernate don't work with Immutables. I want
the ability to have the Immutables builders part of the ORM
layer to keep consistency.

## What is this solution?

Basically uses annotations to identify the DDL parts of the
entity we want to save or read. Though I have no great plans
for queries or complex objects.

## What were other options?

Modify hibernate to work with Immutables. Or just forget it
and work the java sql library like a schmuck. (Yeah, ex-Sun folks,
it's true.)

I'm open to other options.

## Will this ever be finished?

Probably not. I'll poke at the project from time to time
but don't expect greatness.