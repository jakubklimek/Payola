package cz.payola.data.sparql.providers

import cz.payola.data.sparql.{SparqlQueryExecutor, RdfDataProvider}

class AggregateRdfDataProvider(val subProviders: List[RdfDataProvider]) extends RdfDataProvider
{
    def executeSparqlQuery(sparqlQuery: String, executor: SparqlQueryExecutor): Int = {
        subProviders.map(_.executeSparqlQuery(sparqlQuery, executor)).sum
    }
}