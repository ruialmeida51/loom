# Navigation

Adapted from Plannot `ai/PATTERNS_NAVIGATION.md`.

Hosts own serializable typed routes, back stacks and shared ViewModels. Each distinct navigable
workflow owns its Screen/contract/VM. Ordinary filters and create/edit variants can share a form.
Use HorizontalSlideNavDisplay within feature flows and DefaultNavDisplay for peer roots.
System/predictive Back and toolbar Back use the same direction. Back on peer roots returns Home;
only Home exits. Sheet cancel/Back never commits a draft or removes the parent form.
Close a sheet before navigating. Insets belong to the host toolbar/scaffold and are consumed once.
