/**
 * Configuration specific to CPAN repositories.
 */
Ext.define('NX.coreui.view.repository.facet.CpanFacet', {
  extend: 'Ext.form.FieldContainer',
  alias: 'widget.nx-coreui-repository-cpan-facet',
  requires: [
    'NX.I18n'
  ],
  /**
   * @override
   */
  initComponent: function() {
    var me = this;

    me.items = [
      {
        xtype: 'fieldset',
        cls: 'nx-form-section',
        title: NX.I18n.get('Repository_Facet_CpanFacet_Title')
      }
    ];

    me.callParent();
  }

});