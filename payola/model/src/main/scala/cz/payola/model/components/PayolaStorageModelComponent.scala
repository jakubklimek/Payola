package cz.payola.model.components

import cz.payola.domain.RdfStorageComponent
import cz.payola.domain.entities.User
import cz.payola.domain.entities.plugins.concrete.data.PayolaStorage
import cz.payola.domain.entities.plugins.DataSource
import cz.payola.model.ModelException

trait PayolaStorageModelComponent
{
    self: RdfStorageComponent with PluginModelComponent with DataSourceModelComponent =>

    lazy val payolaStorageModel = new
    {
        /**
          * Creates a private storage for the specified user.
          * @param user The owner of the private storage.
          * @return A data source corresponding to the private storage.
          */
        def createUsersPrivateStorage(user: User): DataSource = {
            val plugin = pluginModel.getByName(PayolaStorage.pluginName).getOrElse {
                throw new ModelException("The PayolaStorage plugin doesn't exist.")
            }

            // Create the "users database" in the rdf storage.
            val groupURI = user.id
            rdfStorage.createGroup(groupURI)

            // Create the corresponding data source.
            val instance = plugin.createInstance().setParameter(PayolaStorage.groupURIParameterName, groupURI)
            val dataSource = DataSource("Private Storage of " + user.name, Some(user), instance)
            dataSourceModel.persist(dataSource)
            dataSource
        }
    }
}