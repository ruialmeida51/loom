package com.eyther.lumbridge.features.home.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.home.domain.model.HomeOverviewDomain

data class HomeState(val overview: HomeOverviewDomain? = null, val error: TextResource? = null)
