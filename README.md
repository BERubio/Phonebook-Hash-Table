# Phonebook-Hash-Table
Implemented an abstraction over a phonebook; a collections of pairs of type &lt;Full_Name,Phone_Number>. Phonebook supports both name-based search and phone-based search.

To make both types of searches efficient, phonebook internally maintains a pair of hash tables from Strings to Strings: One will have the person's name as a key and the phone number as a value, and the other one will have the phone number as a key and the name as a value! 

Collision resolution methods include: Separate Chaining, Linear Probing, Ordered Linear Probing and Quadratic Probing. 
